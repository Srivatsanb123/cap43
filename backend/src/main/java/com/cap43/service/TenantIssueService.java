package com.cap43.service;

import com.cap43.dto.*;
import com.cap43.entity.Issue;
import com.cap43.entity.IssueStatusHistory;
import com.cap43.repository.IssueRepository;
import com.cap43.repository.IssueStatusHistoryRepository;
import com.cap43.repository.LeaseRepository;
import com.cap43.security.AuthUser;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TenantIssueService {

    private static final int MAX_ISSUES_PER_HOUR = 10;

    private final IssueRepository issueRepo;
    private final LeaseRepository leaseRepo;
    private final IssueStatusHistoryRepository historyRepo;
    private final SeverityService severityService;
    private final IssueHistoryService history;
    private final IssueAssembler assembler;

    @Transactional
    public TenantIssueView create(AuthUser auth, CreateIssueRequest req) {
        Integer tenantId = requireTenant(auth);
        LocalDateTime now = LocalDateTime.now();

        if (issueRepo.countByTenantIdAndCreatedAtAfter(tenantId, now.minusHours(1)) >= MAX_ISSUES_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many issues submitted recently. Please try again later.");
        }

        
        var lease = leaseRepo.findFirstByTenantIdAndStatusOrderByStartDateDesc(tenantId, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active lease found for this account"));

        SeverityService.Decision d = severityService.classify(req.category(), req.title(), req.description());

        Issue issue = new Issue();
        issue.setTenantId(tenantId);
        issue.setUnitId(lease.getUnitId());
        issue.setCategory(req.category());
        issue.setTitle(req.title().trim());
        issue.setDescription(req.description().trim());
        issue.setSeverity(d.severity());
        issue.setSeveritySource("AUTO");
        issue.setStatus("OPEN");
        issue.setEscalationLevel(0);
        issue.setCreatedAt(now);
        issue.setSlaDueAt(now.plusHours(severityService.slaHours(d.severity())));
        Issue saved = issueRepo.save(issue);

        String reason = d.matchedKeyword() == null
                ? "Issue created; no keyword matched, default severity applied"
                : "Issue created; classified by keyword '" + d.matchedKeyword() + "'";
        history.record(saved.getIssueId(), null, "OPEN", null, d.severity(), auth.userId(), reason);
        return assembler.tenantView(saved, now);
    }

    @Transactional(readOnly = true)
    public List<TenantIssueView> list(AuthUser auth) {
        Integer tenantId = requireTenant(auth);
        LocalDateTime now = LocalDateTime.now();
        return issueRepo.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(i -> assembler.tenantView(i, now)).toList();
    }

    @Transactional(readOnly = true)
    public TenantIssueDetail get(AuthUser auth, Integer issueId) {
        Integer tenantId = requireTenant(auth);
        
        Issue issue = issueRepo.findById(issueId)
                .filter(i -> tenantId.equals(i.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));

        List<TimelineEntry> timeline = historyRepo.findByIssueIdOrderByChangedAtAsc(issueId).stream()
                .filter(h -> h.getNewStatus() != null && !h.getNewStatus().equals(h.getOldStatus()))
                .map((IssueStatusHistory h) -> new TimelineEntry(h.getChangedAt(), h.getNewStatus()))
                .toList();
        return new TenantIssueDetail(assembler.tenantView(issue, LocalDateTime.now()), timeline);
    }

    private static Integer requireTenant(AuthUser auth) {
        if (auth.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not linked to a tenant");
        }
        return auth.tenantId();
    }
}
