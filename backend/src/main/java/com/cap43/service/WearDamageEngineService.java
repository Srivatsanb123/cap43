package com.cap43.service;

import com.cap43.entity.InspectionItem;
import com.cap43.entity.WearDamageRule;
import com.cap43.repository.EvidenceFileRepository;
import com.cap43.repository.SystemConfigRepository;
import com.cap43.repository.WearDamageRuleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WearDamageEngineService {

    public record ClassificationResult(
            String classification, 
            Integer appliedRuleId,
            String ruleName,
            String reasonCode,
            String reasonText,
            String ruleTraceJson
    ) {}

    private final WearDamageRuleRepository ruleRepo;
    private final EvidenceFileRepository evidenceRepo;
    private final SystemConfigRepository configRepo;

    public ClassificationResult evaluate(InspectionItem moveOutItem, InspectionItem moveInItem) {
        boolean requireMoveInBaseline = Boolean.parseBoolean(
                configRepo.findById("dispute.require_move_in_baseline")
                        .map(c -> c.getConfigValue().trim())
                        .orElse("true"));

        int minMoveOutPhotos = Integer.parseInt(
                configRepo.findById("dispute.min_move_out_photos")
                        .map(c -> c.getConfigValue().trim())
                        .orElse("1"));

        long moveOutPhotos = evidenceRepo.countByInspectionItemId(moveOutItem.getItemId());

        
        if (moveOutPhotos < minMoveOutPhotos) {
            return new ClassificationResult(
                    "NEEDS_HUMAN_REVIEW",
                    null,
                    null,
                    "INSUFFICIENT_EVIDENCE",
                    "Fewer than " + minMoveOutPhotos + " move-out photos found (" + moveOutPhotos + " available). Requires human verification.",
                    String.format("{\"reason_code\":\"INSUFFICIENT_EVIDENCE\",\"move_out_photos\":%d,\"required_photos\":%d}", moveOutPhotos, minMoveOutPhotos)
            );
        }

        
        if (moveInItem == null && requireMoveInBaseline) {
            String cause = moveOutItem.getCauseNoted();
            if (cause == null || "UNKNOWN".equalsIgnoreCase(cause) || cause.isBlank()) {
                return new ClassificationResult(
                        "NEEDS_HUMAN_REVIEW",
                        null,
                        null,
                        "MISSING_MOVE_IN_BASELINE",
                        "No move-in inspection record exists for " + moveOutItem.getItemName() + " and the cause of damage is recorded as " + (cause == null ? "NOT_SPECIFIED" : cause) + ". The system cannot decide automatically and must route to human review.",
                        String.format("{\"reason_code\":\"MISSING_MOVE_IN_BASELINE\",\"baseline_found\":false,\"cause_noted\":\"%s\",\"move_out_photos\":%d}", cause == null ? "UNKNOWN" : cause, moveOutPhotos)
                );
            }
        }

        
        List<WearDamageRule> rules = ruleRepo.findByItemCategoryAndActiveTrueOrderByPriorityAsc(moveOutItem.getItemCategory());
        if (rules.isEmpty()) {
            rules = ruleRepo.findByActiveTrueOrderByPriorityAsc();
        }

        for (WearDamageRule r : rules) {
            if (matches(r, moveOutItem, moveInItem)) {
                String trace = String.format("{\"rule_id\":%d,\"rule_name\":\"%s\",\"matched_classification\":\"%s\",\"baseline_found\":%b,\"move_out_photos\":%d}",
                        r.getRuleId(), r.getRuleName().replace("\"", "'"), r.getClassification(), moveInItem != null, moveOutPhotos);
                return new ClassificationResult(
                        r.getClassification(),
                        r.getRuleId(),
                        r.getRuleName(),
                        null,
                        null,
                        trace
                );
            }
        }

        
        return new ClassificationResult(
                "NEEDS_HUMAN_REVIEW",
                null,
                null,
                "NO_MATCHING_RULE",
                "No explicit wear-and-tear or damage rule matched item " + moveOutItem.getItemName() + " (Category: " + moveOutItem.getItemCategory() + ", Condition: " + moveOutItem.getConditionRating() + ").",
                String.format("{\"reason_code\":\"NO_MATCHING_RULE\",\"item_category\":\"%s\",\"move_out_condition\":\"%s\"}", moveOutItem.getItemCategory(), moveOutItem.getConditionRating())
        );
    }

    private boolean matches(WearDamageRule r, InspectionItem moveOut, InspectionItem moveIn) {
        if (r.getItemCategory() != null && !r.getItemCategory().equalsIgnoreCase(moveOut.getItemCategory())) {
            return false;
        }

        if (r.getMinAgeYears() != null) {
            if (moveOut.getItemAgeYears() == null || moveOut.getItemAgeYears() < r.getMinAgeYears()) {
                return false;
            }
        }

        if (r.getMaxAgeYears() != null) {
            if (moveOut.getItemAgeYears() == null || moveOut.getItemAgeYears() > r.getMaxAgeYears()) {
                return false;
            }
        }

        if (r.getMoveOutCondition() != null) {
            if (moveOut.getConditionRating() == null || !r.getMoveOutCondition().equalsIgnoreCase(moveOut.getConditionRating())) {
                return false;
            }
        }

        if (r.getMoveInCondition() != null) {
            if (moveIn == null || moveIn.getConditionRating() == null || !r.getMoveInCondition().equalsIgnoreCase(moveIn.getConditionRating())) {
                return false;
            }
        }

        if (r.getCause() != null) {
            if (moveOut.getCauseNoted() == null || !r.getCause().equalsIgnoreCase(moveOut.getCauseNoted())) {
                return false;
            }
        }

        return true;
    }
}

