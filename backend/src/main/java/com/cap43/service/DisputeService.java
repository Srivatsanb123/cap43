package com.cap43.service;

import com.cap43.dto.*;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DepositDisputeRepository disputeRepo;
    private final DisputeItemRepository itemRepo;
    private final DisputeEventRepository eventRepo;
    private final ReviewQueueRepository reviewRepo;
    private final LeaseRepository leaseRepo;
    private final UnitRepository unitRepo;
    private final PropertyRepository propertyRepo;
    private final TenantRepository tenantRepo;
    private final InspectionItemRepository inspectionItemRepo;
    private final WearDamageRuleRepository ruleRepo;
    private final EvidenceFileRepository evidenceRepo;
    private final WearDamageEngineService engine;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<DisputeRow> listDisputes(String status) {
        List<DepositDispute> disputes = (status == null || status.isBlank())
                ? disputeRepo.findAllByOrderByOpenedAtDesc()
                : disputeRepo.findByStatusOrderByOpenedAtDesc(status);

        if (disputes.isEmpty()) return List.of();

        List<Integer> disputeIds = disputes.stream().map(DepositDispute::getDisputeId).toList();
        Map<Integer, List<DisputeItem>> itemsByDispute = itemRepo.findByDisputeIdIn(disputeIds).stream()
                .collect(Collectors.groupingBy(DisputeItem::getDisputeId));

        Map<Integer, Lease> leaseMap = leaseRepo.findAllById(disputes.stream().map(DepositDispute::getLeaseId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Lease::getLeaseId, Function.identity()));

        Map<Integer, Unit> unitMap = unitRepo.findAllById(leaseMap.values().stream().map(Lease::getUnitId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Unit::getUnitId, Function.identity()));

        Map<Integer, Property> propertyMap = propertyRepo.findAllById(unitMap.values().stream().map(Unit::getPropertyId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Property::getPropertyId, Function.identity()));

        Map<Integer, Tenant> tenantMap = tenantRepo.findAllById(disputes.stream().map(DepositDispute::getTenantId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Tenant::getTenantId, Function.identity()));

        return disputes.stream().map(d -> {
            Lease l = leaseMap.get(d.getLeaseId());
            Unit u = l != null ? unitMap.get(l.getUnitId()) : null;
            Property p = u != null ? propertyMap.get(u.getPropertyId()) : null;
            Tenant t = tenantMap.get(d.getTenantId());
            List<DisputeItem> items = itemsByDispute.getOrDefault(d.getDisputeId(), List.of());

            BigDecimal totalClaimed = items.stream()
                    .map(DisputeItem::getClaimedAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new DisputeRow(
                    d.getDisputeId(),
                    d.getLeaseId(),
                    d.getTenantId(),
                    t != null ? t.getTenantRef() : null,
                    p != null ? p.getName() : null,
                    u != null ? u.getUnitNumber() : null,
                    d.getDepositHeld(),
                    d.getStatus(),
                    items.size(),
                    totalClaimed,
                    d.getOpenedAt(),
                    d.getResolvedAt()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeRow> listForTenant(AuthUser tenantUser) {
        if (tenantUser.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not linked to a tenant");
        }
        return listDisputes(null).stream()
                .filter(d -> tenantUser.tenantId().equals(d.tenantId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DisputeDetailView getDispute(Integer id) {
        DepositDispute d = disputeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute not found"));

        Lease l = leaseRepo.findById(d.getLeaseId()).orElse(null);
        Unit u = l != null ? unitRepo.findById(l.getUnitId()).orElse(null) : null;
        Property p = u != null ? propertyRepo.findById(u.getPropertyId()).orElse(null) : null;
        Tenant t = tenantRepo.findById(d.getTenantId()).orElse(null);

        List<DisputeItem> items = itemRepo.findByDisputeIdOrderByDisputeItemIdAsc(id);
        List<DisputeEvent> events = eventRepo.findByDisputeIdOrderByCreatedAtAsc(id);

        Map<Integer, WearDamageRule> ruleMap = ruleRepo.findAll().stream()
                .collect(Collectors.toMap(WearDamageRule::getRuleId, Function.identity()));

        List<DisputeItemView> itemViews = items.stream().map(it -> {
            InspectionItem outItem = inspectionItemRepo.findById(it.getMoveOutItemId()).orElse(null);
            InspectionItem inItem = it.getMoveInItemId() != null ? inspectionItemRepo.findById(it.getMoveInItemId()).orElse(null) : null;
            WearDamageRule r = it.getAppliedRuleId() != null ? ruleMap.get(it.getAppliedRuleId()) : null;

            long outPhotos = evidenceRepo.countByInspectionItemId(it.getMoveOutItemId());
            long inPhotos = it.getMoveInItemId() != null ? evidenceRepo.countByInspectionItemId(it.getMoveInItemId()) : 0L;

            return new DisputeItemView(
                    it.getDisputeItemId(),
                    it.getDisputeId(),
                    it.getMoveOutItemId(),
                    outItem != null ? outItem.getItemName() : "Item #" + it.getMoveOutItemId(),
                    outItem != null ? outItem.getItemCategory() : null,
                    outItem != null ? outItem.getConditionRating() : null,
                    outItem != null ? outItem.getCauseNoted() : null,
                    it.getMoveInItemId(),
                    inItem != null ? inItem.getConditionRating() : null,
                    it.getClaimedAmount(),
                    it.getTenantPosition(),
                    it.getTenantComment(),
                    it.getSystemClassification(),
                    it.getAppliedRuleId(),
                    r != null ? r.getRuleName() : null,
                    it.getRuleTrace(),
                    it.getOperatorDecision(),
                    it.getDecidedAmount(),
                    it.getDecidedAt(),
                    it.getDecisionNotes(),
                    outPhotos,
                    inPhotos
            );
        }).toList();

        List<DisputeEventView> eventViews = events.stream().map(e -> new DisputeEventView(
                e.getEventId(),
                e.getDisputeId(),
                e.getDisputeItemId(),
                e.getEventType(),
                e.getActorType(),
                e.getActorUserId(),
                e.getDetail(),
                e.getCreatedAt()
        )).toList();

        return new DisputeDetailView(
                d.getDisputeId(),
                d.getLeaseId(),
                d.getTenantId(),
                t != null ? t.getTenantRef() : null,
                p != null ? p.getName() : null,
                u != null ? u.getUnitNumber() : null,
                d.getDepositHeld(),
                d.getStatus(),
                d.getOpenedAt(),
                d.getResolvedAt(),
                itemViews,
                eventViews
        );
    }

    @Transactional
    public DisputeDetailView createDispute(CreateDisputeClaimRequest req, AuthUser admin, String ip) {
        Lease lease = leaseRepo.findById(req.leaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lease not found"));

        DepositDispute dispute = new DepositDispute();
        dispute.setLeaseId(lease.getLeaseId());
        dispute.setTenantId(lease.getTenantId());
        dispute.setDepositHeld(lease.getDepositAmount());
        dispute.setStatus("OPEN");
        dispute.setOpenedAt(LocalDateTime.now());
        DepositDispute savedDispute = disputeRepo.save(dispute);

        logEvent(savedDispute.getDisputeId(), null, "DISPUTE_OPENED", "ADMIN", admin.userId(),
                "Claim raised with " + req.items().size() + " items");

        boolean anyNeedsHumanReview = false;

        for (CreateDisputeClaimRequest.ClaimItemRequest itemReq : req.items()) {
            InspectionItem moveOut = inspectionItemRepo.findById(itemReq.moveOutItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move-out item not found: " + itemReq.moveOutItemId()));
            InspectionItem moveIn = itemReq.moveInItemId() != null
                    ? inspectionItemRepo.findById(itemReq.moveInItemId()).orElse(null)
                    : null;

            WearDamageEngineService.ClassificationResult result = engine.evaluate(moveOut, moveIn);

            DisputeItem item = new DisputeItem();
            item.setDisputeId(savedDispute.getDisputeId());
            item.setMoveOutItemId(moveOut.getItemId());
            item.setMoveInItemId(moveIn != null ? moveIn.getItemId() : null);
            item.setClaimedAmount(itemReq.claimedAmount());
            item.setTenantPosition("NO_RESPONSE");
            item.setSystemClassification(result.classification());
            item.setAppliedRuleId(result.appliedRuleId());
            item.setRuleTrace(result.ruleTraceJson());
            item.setCreatedAt(LocalDateTime.now());
            DisputeItem savedItem = itemRepo.save(item);

            if ("NEEDS_HUMAN_REVIEW".equals(result.classification())) {
                anyNeedsHumanReview = true;
                ReviewQueue rq = new ReviewQueue();
                rq.setDisputeItemId(savedItem.getDisputeItemId());
                rq.setReasonCode(result.reasonCode());
                rq.setReasonText(result.reasonText());
                rq.setStatus("PENDING");
                rq.setCreatedAt(LocalDateTime.now());
                reviewRepo.save(rq);

                logEvent(savedDispute.getDisputeId(), savedItem.getDisputeItemId(), "ROUTED_TO_HUMAN_REVIEW", "SYSTEM", null,
                        result.reasonCode() + ": " + result.reasonText());
            } else {
                logEvent(savedDispute.getDisputeId(), savedItem.getDisputeItemId(), "ITEM_CLASSIFIED", "SYSTEM", null,
                        "Classified " + result.classification() + " by rule " + result.appliedRuleId());
            }
        }

        if (anyNeedsHumanReview) {
            savedDispute.setStatus("AWAITING_HUMAN_REVIEW");
            disputeRepo.save(savedDispute);
        }

        audit.log(admin.userId(), "DISPUTE_CREATE", "DEPOSIT_DISPUTE", String.valueOf(savedDispute.getDisputeId()),
                "Created dispute for lease " + lease.getLeaseId(), ip);

        return getDispute(savedDispute.getDisputeId());
    }

    @Transactional
    public DisputeDetailView contestItem(Integer disputeId, Integer itemId, ContestDisputeItemRequest req, AuthUser tenant, String ip) {
        DepositDispute dispute = disputeRepo.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute not found"));

        if (tenant.tenantId() == null || !tenant.tenantId().equals(dispute.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized for this dispute");
        }

        DisputeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute item not found"));

        if (!dispute.getDisputeId().equals(item.getDisputeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this dispute");
        }

        item.setTenantPosition(req.position());
        item.setTenantComment(req.comment() == null || req.comment().isBlank() ? null : req.comment().trim());
        itemRepo.save(item);

        logEvent(disputeId, itemId, "TENANT_CONTESTED", "TENANT", tenant.userId(),
                "Tenant position: " + req.position() + (req.comment() != null ? " - " + req.comment() : ""));

        audit.log(tenant.userId(), "DISPUTE_CONTEST", "DISPUTE_ITEM", String.valueOf(itemId),
                "Tenant updated position to " + req.position(), ip);

        return getDispute(disputeId);
    }

    @Transactional
    public DisputeDetailView decideItem(Integer disputeId, Integer itemId, DecideDisputeItemRequest req, AuthUser admin, String ip) {
        DepositDispute dispute = disputeRepo.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute not found"));

        DisputeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispute item not found"));

        if (!dispute.getDisputeId().equals(item.getDisputeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this dispute");
        }

        item.setOperatorDecision(req.operatorDecision());
        item.setDecidedAmount(req.decidedAmount());
        item.setDecidedByUserId(admin.userId());
        item.setDecidedAt(LocalDateTime.now());
        item.setDecisionNotes(req.decisionNotes());
        itemRepo.save(item);

        
        reviewRepo.findByDisputeItemId(itemId).ifPresent(rq -> {
            rq.setStatus("DECIDED");
            rq.setDecidedAt(LocalDateTime.now());
            rq.setDecidedByUserId(admin.userId());
            rq.setOutcomeNotes("Decided via dispute item: " + req.operatorDecision() + " ($" + req.decidedAmount() + ")");
            reviewRepo.save(rq);
        });

        logEvent(disputeId, itemId, "OPERATOR_DECISION", "ADMIN", admin.userId(),
                req.operatorDecision() + " to " + req.decidedAmount() + (req.decisionNotes() != null ? " (" + req.decisionNotes() + ")" : ""));

        
        List<DisputeItem> allItems = itemRepo.findByDisputeIdOrderByDisputeItemIdAsc(disputeId);
        boolean allDecided = allItems.stream().allMatch(it -> it.getOperatorDecision() != null);
        if (allDecided) {
            dispute.setStatus("RESOLVED");
            dispute.setResolvedAt(LocalDateTime.now());
            disputeRepo.save(dispute);
            logEvent(disputeId, null, "DISPUTE_RESOLVED", "ADMIN", admin.userId(), "All items decided and dispute resolved.");
        }

        audit.log(admin.userId(), "DISPUTE_DECISION", "DISPUTE_ITEM", String.valueOf(itemId),
                req.operatorDecision() + " amount " + req.decidedAmount(), ip);

        return getDispute(disputeId);
    }

    @Transactional(readOnly = true)
    public List<ReviewQueueRow> listReviewQueue(String status) {
        List<ReviewQueue> queue = (status == null || status.isBlank())
                ? reviewRepo.findAllByOrderByCreatedAtDesc()
                : reviewRepo.findByStatusOrderByCreatedAtDesc(status);

        return queue.stream().map(q -> {
            Integer disputeId = null;
            if (q.getDisputeItemId() != null) {
                disputeId = itemRepo.findById(q.getDisputeItemId()).map(DisputeItem::getDisputeId).orElse(null);
            }
            return new ReviewQueueRow(
                    q.getReviewId(),
                    q.getDisputeItemId(),
                    disputeId,
                    q.getIssueId(),
                    q.getReasonCode(),
                    q.getReasonText(),
                    q.getStatus(),
                    q.getAssignedToUserId(),
                    q.getCreatedAt(),
                    q.getDecidedAt(),
                    q.getOutcomeNotes()
            );
        }).toList();
    }

    @Transactional
    public ReviewQueueRow resolveReview(Integer reviewId, ResolveReviewRequest req, AuthUser admin, String ip) {
        ReviewQueue rq = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review item not found"));

        rq.setStatus("DECIDED");
        rq.setDecidedAt(LocalDateTime.now());
        rq.setDecidedByUserId(admin.userId());
        rq.setOutcomeNotes(req.outcomeNotes());
        ReviewQueue saved = reviewRepo.save(rq);

        if (saved.getDisputeItemId() != null) {
            itemRepo.findById(saved.getDisputeItemId()).ifPresent(item -> {
                logEvent(item.getDisputeId(), item.getDisputeItemId(), "HUMAN_REVIEW_RESOLVED", "ADMIN", admin.userId(),
                        "Review resolved: " + req.outcomeNotes());
            });
        }

        audit.log(admin.userId(), "REVIEW_RESOLVED", "REVIEW_QUEUE", String.valueOf(reviewId), req.outcomeNotes(), ip);

        Integer dispId = saved.getDisputeItemId() != null
                ? itemRepo.findById(saved.getDisputeItemId()).map(DisputeItem::getDisputeId).orElse(null)
                : null;

        return new ReviewQueueRow(
                saved.getReviewId(),
                saved.getDisputeItemId(),
                dispId,
                saved.getIssueId(),
                saved.getReasonCode(),
                saved.getReasonText(),
                saved.getStatus(),
                saved.getAssignedToUserId(),
                saved.getCreatedAt(),
                saved.getDecidedAt(),
                saved.getOutcomeNotes()
        );
    }

    @Transactional(readOnly = true)
    public List<WearDamageRuleView> listWearDamageRules() {
        return ruleRepo.findAll().stream()
                .sorted(Comparator.comparing(WearDamageRule::getPriority))
                .map(r -> new WearDamageRuleView(
                        r.getRuleId(),
                        r.getRuleName(),
                        r.getItemCategory(),
                        r.getMinAgeYears(),
                        r.getMaxAgeYears(),
                        r.getMoveInCondition(),
                        r.getMoveOutCondition(),
                        r.getCause(),
                        r.getClassification(),
                        r.getPriority(),
                        r.isActive(),
                        r.getNotes()
                )).toList();
    }

    private void logEvent(Integer disputeId, Integer disputeItemId, String eventType, String actorType, Integer actorUserId, String detail) {
        DisputeEvent e = new DisputeEvent();
        e.setDisputeId(disputeId);
        e.setDisputeItemId(disputeItemId);
        e.setEventType(eventType);
        e.setActorType(actorType);
        e.setActorUserId(actorUserId);
        e.setDetail(detail);
        e.setCreatedAt(LocalDateTime.now());
        eventRepo.save(e);
    }
}

