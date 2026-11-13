package com.cap43.service;

import com.cap43.common.SlaUtil;
import com.cap43.dto.*;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminIssueService {

    private static final Set<String> ACTIVE_WORK_ORDER = Set.of("ASSIGNED", "ACCEPTED", "IN_PROGRESS");

    private final IssueRepository issueRepo;
    private final IssueStatusHistoryRepository historyRepo;
    private final WorkOrderRepository workOrderRepo;
    private final EscalationRepository escalationRepo;
    private final VendorRepository vendorRepo;
    private final SeverityService severityService;
    private final IssueHistoryService history;
    private final IssueAssembler assembler;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<AdminIssueRow> list(String status, String severity, boolean overdueOnly) {
        List<AdminIssueRow> rows = assembler.adminRows(issueRepo.search(blank(status), blank(severity)));
        return overdueOnly ? rows.stream().filter(AdminIssueRow::overdue).toList() : rows;
    }

    @Transactional(readOnly = true)
    public AdminIssueDetail detail(Integer id) {
        Issue issue = find(id);

        List<HistoryEntry> hist = historyRepo.findByIssueIdOrderByChangedAtAsc(id).stream()
                .map(h -> new HistoryEntry(h.getChangedAt(), h.getOldStatus(), h.getNewStatus(),
                        h.getOldSeverity(), h.getNewSeverity(), h.getChangeReason(), h.getChangedByUserId()))
                .toList();

        List<WorkOrder> wos = workOrderRepo.findByIssueIdOrderByAssignedAtAsc(id);
        var vendors = vendorRepo.findAllById(wos.stream().map(WorkOrder::getVendorId).toList());
        List<WorkOrderView> woViews = wos.stream().map(w -> toView(w,
                vendors.stream().filter(v -> v.getVendorId().equals(w.getVendorId()))
                        .map(Vendor::getCompanyName).findFirst().orElse(null))).toList();

        List<EscalationView> escs = escalationRepo.findByIssueIdOrderByLevelAsc(id).stream()
                .map(e -> new EscalationView(e.getEscalationId(), e.getLevel(), e.getReason(),
                        e.getTriggeredAt(), e.getAcknowledgedAt()))
                .toList();

        return new AdminIssueDetail(assembler.adminRow(issue), issue.getDescription(), hist, woViews, escs);
    }

    
    @Transactional
    public AdminIssueRow changeSeverity(Integer id, ChangeSeverityRequest req, AuthUser admin, String ip) {
        Issue issue = find(id);
        if ("RESOLVED".equals(issue.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resolved issues cannot be re-prioritised");
        }
        if (req.severity().equals(issue.getSeverity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Severity is unchanged");
        }

        int hours = severityService.slaHours(req.severity());
        LocalDateTime now = LocalDateTime.now();
        boolean wasOverdue = SlaUtil.isOverdue(issue, now);
        String old = issue.getSeverity();

        issue.setSeverity(req.severity());
        issue.setSeveritySource("ADMIN");
        if (!wasOverdue) {
            LocalDateTime due = issue.getCreatedAt().plusHours(hours);
            if (!due.isAfter(now)) due = now.plusHours(hours);
            issue.setSlaDueAt(due);
        }
        issueRepo.save(issue);

        history.record(id, issue.getStatus(), issue.getStatus(), old, req.severity(), admin.userId(), req.reason());
        audit.log(admin.userId(), "SEVERITY_CHANGE", "ISSUE", String.valueOf(id),
                old + " -> " + req.severity() + ": " + req.reason(), ip);
        return assembler.adminRow(issue);
    }

    @Transactional
    public WorkOrderView assignVendor(Integer issueId, AssignVendorRequest req, AuthUser admin, String ip) {
        Issue issue = find(issueId);
        if ("RESOLVED".equals(issue.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Issue is already resolved");
        }
        Vendor vendor = vendorRepo.findById(req.vendorId()).filter(Vendor::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown or inactive vendor"));
        if (workOrderRepo.existsByIssueIdAndStatusIn(issueId, ACTIVE_WORK_ORDER)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This issue already has an active work order");
        }

        WorkOrder wo = new WorkOrder();
        wo.setIssueId(issueId);
        wo.setVendorId(vendor.getVendorId());
        wo.setStatus("ASSIGNED");
        wo.setAssignedAt(LocalDateTime.now());
        wo.setAssignedByUserId(admin.userId());
        wo.setVendorNotes(req.notes() == null || req.notes().isBlank() ? null : req.notes().trim());
        WorkOrder saved = workOrderRepo.save(wo);

        if ("OPEN".equals(issue.getStatus())) {
            issue.setStatus("ASSIGNED");
            issueRepo.save(issue);
            history.record(issueId, "OPEN", "ASSIGNED", issue.getSeverity(), issue.getSeverity(),
                    admin.userId(), "Vendor assigned: " + vendor.getCompanyName());
        }
        audit.log(admin.userId(), "VENDOR_ASSIGN", "ISSUE", String.valueOf(issueId),
                "Work order " + saved.getWorkOrderId() + " to " + vendor.getCompanyName(), ip);
        return toView(saved, vendor.getCompanyName());
    }

    private WorkOrderView toView(WorkOrder w, String vendorName) {
        return new WorkOrderView(w.getWorkOrderId(), w.getVendorId(), vendorName, w.getStatus(),
                w.getAssignedAt(), w.getAcceptedAt(), w.getStartedAt(), w.getCompletedAt(), w.getVendorNotes());
    }

    private Issue find(Integer id) {
        return issueRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));
    }

    private static String blank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
