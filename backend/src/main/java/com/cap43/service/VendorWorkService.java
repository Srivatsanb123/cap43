package com.cap43.service;

import com.cap43.common.SlaUtil;
import com.cap43.dto.VendorWorkOrderView;
import com.cap43.dto.WorkOrderUpdateRequest;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
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
public class VendorWorkService {

    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "ASSIGNED", Set.of("ACCEPTED", "REJECTED"),
            "ACCEPTED", Set.of("IN_PROGRESS", "REJECTED"),
            "IN_PROGRESS", Set.of("COMPLETED"));

    private final WorkOrderRepository workOrderRepo;
    private final WorkOrderUpdateRepository updateRepo;
    private final IssueRepository issueRepo;
    private final UnitRepository unitRepo;
    private final PropertyRepository propertyRepo;
    private final IssueHistoryService history;

    @Transactional(readOnly = true)
    public List<VendorWorkOrderView> list(AuthUser auth) {
        Integer vendorId = requireVendor(auth);
        return toViews(workOrderRepo.findByVendorIdOrderByAssignedAtDesc(vendorId));
    }

    @Transactional
    public VendorWorkOrderView update(AuthUser auth, Integer workOrderId, WorkOrderUpdateRequest req) {
        Integer vendorId = requireVendor(auth);
        
        WorkOrder wo = workOrderRepo.findById(workOrderId)
                .filter(w -> vendorId.equals(w.getVendorId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found"));

        String next = req.status();
        if (!ALLOWED.getOrDefault(wo.getStatus(), Set.of()).contains(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move a work order from " + wo.getStatus() + " to " + next);
        }
        boolean hasNote = req.note() != null && !req.note().isBlank();
        if ("REJECTED".equals(next) && !hasNote) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A note is required when rejecting");
        }

        LocalDateTime now = LocalDateTime.now();
        wo.setStatus(next);
        if (hasNote) wo.setVendorNotes(req.note().trim());
        switch (next) {
            case "ACCEPTED" -> wo.setAcceptedAt(now);
            case "IN_PROGRESS" -> wo.setStartedAt(now);
            case "COMPLETED" -> wo.setCompletedAt(now);
            default -> { }
        }
        WorkOrder saved = workOrderRepo.save(wo);

        WorkOrderUpdate u = new WorkOrderUpdate();
        u.setWorkOrderId(saved.getWorkOrderId());
        u.setStatus(next);
        u.setNote(hasNote ? req.note().trim() : null);
        u.setCreatedByUserId(auth.userId());
        u.setCreatedAt(now);
        updateRepo.save(u);

        if (saved.getIssueId() != null) {
            issueRepo.findById(saved.getIssueId()).ifPresent(i -> applyIssueEffects(i, next, req.note(), auth, now));
        }
        return toViews(List.of(saved)).get(0);
    }

    private void applyIssueEffects(Issue issue, String newStatus, String note, AuthUser auth, LocalDateTime now) {
        String oldStatus = issue.getStatus();
        switch (newStatus) {
            case "ACCEPTED" -> {
                if (issue.getAcknowledgedAt() == null) issue.setAcknowledgedAt(now);
            }
            case "IN_PROGRESS" -> {
                if (issue.getAcknowledgedAt() == null) issue.setAcknowledgedAt(now);
                if (!"RESOLVED".equals(oldStatus)) issue.setStatus("IN_PROGRESS");
            }
            case "COMPLETED" -> {
                issue.setStatus("RESOLVED");
                issue.setResolvedAt(now);
            }
            case "REJECTED" -> issue.setStatus("OPEN");   
            default -> { }
        }
        issueRepo.save(issue);

        if (!Objects.equals(oldStatus, issue.getStatus())) {
            String reason = "Vendor set work order " + newStatus
                    + ("REJECTED".equals(newStatus) && note != null ? ": " + note.trim() : "");
            history.record(issue.getIssueId(), oldStatus, issue.getStatus(),
                    issue.getSeverity(), issue.getSeverity(), auth.userId(), reason);
        }
    }

    private List<VendorWorkOrderView> toViews(List<WorkOrder> wos) {
        Map<Integer, Issue> issues = issueRepo
                .findAllById(wos.stream().map(WorkOrder::getIssueId).filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Issue::getIssueId, Function.identity()));
        Map<Integer, Unit> units = unitRepo
                .findAllById(issues.values().stream().map(Issue::getUnitId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
        Map<Integer, Property> props = propertyRepo
                .findAllById(units.values().stream().map(Unit::getPropertyId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Property::getPropertyId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();

        
        return wos.stream().map(w -> {
            Issue i = w.getIssueId() == null ? null : issues.get(w.getIssueId());
            Unit u = i == null ? null : units.get(i.getUnitId());
            Property p = u == null ? null : props.get(u.getPropertyId());
            return new VendorWorkOrderView(w.getWorkOrderId(), w.getIssueId(), w.getStatus(), w.getAssignedAt(),
                    p == null ? null : p.getName(), p == null ? null : p.getAddressLine(),
                    u == null ? null : u.getUnitNumber(),
                    i == null ? "Move-out repair" : i.getTitle(),
                    i == null ? null : i.getDescription(),
                    i == null ? null : i.getSeverity(),
                    i == null ? null : i.getSlaDueAt(),
                    i != null && SlaUtil.isOverdue(i, now),
                    w.getVendorNotes());
        }).toList();
    }

    private static Integer requireVendor(AuthUser auth) {
        if (auth.vendorId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not linked to a vendor");
        }
        return auth.vendorId();
    }
}
