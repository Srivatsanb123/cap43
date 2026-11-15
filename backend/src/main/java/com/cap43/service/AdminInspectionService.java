package com.cap43.service;

import com.cap43.dto.*;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminInspectionService {

    private final LeaseRepository leaseRepo;
    private final InspectionRepository inspectionRepo;
    private final InspectionItemRepository itemRepo;
    private final EvidenceFileRepository evidenceRepo;
    private final AuditService audit;

    @Transactional
    public InspectionView create(Integer leaseId, CreateInspectionRequest req, AuthUser admin, String ip) {
        Lease lease = leaseRepo.findById(leaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lease not found"));
        String type = req.inspectionType();

        if (inspectionRepo.existsByLeaseIdAndInspectionType(leaseId, type)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A " + type + " inspection already exists for this lease");
        }
        if ("MOVE_OUT".equals(type) && !"ENDED".equals(lease.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "End the lease before recording a move-out inspection");
        }
        if (req.inspectedOn().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inspection date cannot be in the future");
        }

        Inspection i = new Inspection();
        i.setLeaseId(leaseId);
        i.setInspectionType(type);
        i.setInspectedOn(req.inspectedOn());
        i.setInspectorUserId(admin.userId());
        i.setNotes(req.notes());
        Inspection saved = inspectionRepo.save(i);

        audit.log(admin.userId(), "INSPECTION_CREATE", "INSPECTION", String.valueOf(saved.getInspectionId()),
                type + " for lease " + leaseId, ip);
        return new InspectionView(saved.getInspectionId(), leaseId, type, saved.getInspectedOn(),
                saved.getNotes(), List.of());
    }

    @Transactional
    public InspectionItemView addItem(Integer inspectionId, CreateItemRequest req, AuthUser admin, String ip) {
        if (!inspectionRepo.existsById(inspectionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection not found");
        }
        InspectionItem it = new InspectionItem();
        it.setInspectionId(inspectionId);
        it.setItemCategory(req.itemCategory());
        it.setItemName(req.itemName().trim());
        it.setLocation(req.location());
        it.setConditionRating(req.conditionRating());
        it.setItemAgeYears(req.itemAgeYears());
        it.setCauseNoted(req.causeNoted());
        it.setRemarks(req.remarks());
        InspectionItem saved = itemRepo.save(it);

        audit.log(admin.userId(), "INSPECTION_ITEM_ADD", "INSPECTION_ITEM", String.valueOf(saved.getItemId()),
                saved.getItemCategory() + " " + saved.getConditionRating(), ip);
        return itemView(saved, 0L);
    }

    @Transactional(readOnly = true)
    public InspectionView get(Integer inspectionId) {
        Inspection i = inspectionRepo.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection not found"));
        return toViews(List.of(i)).get(0);
    }

    @Transactional(readOnly = true)
    public List<InspectionView> listForLease(Integer leaseId) {
        if (!leaseRepo.existsById(leaseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lease not found");
        }
        return toViews(inspectionRepo.findByLeaseIdOrderByInspectedOnAsc(leaseId));
    }

    private List<InspectionView> toViews(List<Inspection> inspections) {
        if (inspections.isEmpty()) return List.of();
        Map<Integer, List<InspectionItem>> items = itemRepo
                .findByInspectionIdIn(inspections.stream().map(Inspection::getInspectionId).toList())
                .stream().collect(Collectors.groupingBy(InspectionItem::getInspectionId));

        Set<Integer> itemIds = items.values().stream().flatMap(List::stream)
                .map(InspectionItem::getItemId).collect(Collectors.toSet());
        Map<Integer, Long> evidenceCounts = itemIds.isEmpty() ? Collections.emptyMap()
                : evidenceRepo.findByInspectionItemIdIn(itemIds).stream()
                    .collect(Collectors.groupingBy(EvidenceFile::getInspectionItemId, Collectors.counting()));

        return inspections.stream().map(i -> new InspectionView(
                i.getInspectionId(), i.getLeaseId(), i.getInspectionType(), i.getInspectedOn(), i.getNotes(),
                items.getOrDefault(i.getInspectionId(), List.of()).stream()
                        .map(it -> itemView(it, evidenceCounts.getOrDefault(it.getItemId(), 0L)))
                        .toList()))
                .toList();
    }

    private InspectionItemView itemView(InspectionItem it, long evidenceCount) {
        return new InspectionItemView(it.getItemId(), it.getInspectionId(), it.getItemCategory(), it.getItemName(),
                it.getLocation(), it.getConditionRating(), it.getItemAgeYears(), it.getCauseNoted(),
                it.getRemarks(), evidenceCount);
    }
}
