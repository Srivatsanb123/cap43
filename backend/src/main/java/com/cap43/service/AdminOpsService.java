package com.cap43.service;

import com.cap43.common.SlaUtil;
import com.cap43.dto.*;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminOpsService {

    private final IssueRepository issueRepo;
    private final EscalationRepository escalationRepo;
    private final VendorRepository vendorRepo;
    private final SeverityRuleRepository ruleRepo;
    private final IssueKeywordRepository keywordRepo;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public DashboardSummary dashboard() {
        List<Issue> all = issueRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        List<Issue> open = all.stream().filter(i -> !"RESOLVED".equals(i.getStatus())).toList();
        long overdue = open.stream().filter(i -> SlaUtil.isOverdue(i, now)).count();
        long escalated = open.stream().filter(i -> i.getEscalationLevel() != null && i.getEscalationLevel() > 0).count();

        Map<String, Long> openBySeverity = open.stream()
                .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));

        List<Issue> resolved = all.stream().filter(i -> "RESOLVED".equals(i.getStatus())).toList();
        long onTime = resolved.stream()
                .filter(i -> i.getResolvedAt() != null && !i.getResolvedAt().isAfter(i.getSlaDueAt())).count();
        Double compliance = resolved.isEmpty() ? null : Math.round(onTime * 1000.0 / resolved.size()) / 10.0;

        return new DashboardSummary(all.size(), open.size(), overdue, escalated,
                escalationRepo.countByAcknowledgedAtIsNull(), openBySeverity, byStatus, compliance);
    }

    @Transactional(readOnly = true)
    public List<EscalationRow> openEscalations() {
        List<Escalation> escs = escalationRepo.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();
        Map<Integer, Issue> issues = issueRepo
                .findAllById(escs.stream().map(Escalation::getIssueId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Issue::getIssueId, Function.identity()));
        return escs.stream().map(e -> {
            Issue i = issues.get(e.getIssueId());
            return new EscalationRow(e.getEscalationId(), e.getIssueId(),
                    i == null ? null : i.getTitle(), i == null ? null : i.getSeverity(),
                    e.getLevel(), e.getReason(), e.getTriggeredAt());
        }).toList();
    }

    @Transactional
    public void acknowledgeEscalation(Integer id, AuthUser admin, String ip) {
        Escalation e = escalationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escalation not found"));
        if (e.getAcknowledgedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Escalation already acknowledged");
        }
        e.setAcknowledgedAt(LocalDateTime.now());
        e.setAcknowledgedByUserId(admin.userId());
        escalationRepo.save(e);
        audit.log(admin.userId(), "ESCALATION_ACK", "ESCALATION", String.valueOf(id), null, ip);
    }

    @Transactional(readOnly = true)
    public List<VendorOption> vendors() {
        return vendorRepo.findAll().stream().filter(Vendor::isActive)
                .map(v -> new VendorOption(v.getVendorId(), v.getCompanyName(), v.getTrade())).toList();
    }

    @Transactional(readOnly = true)
    public List<SeverityRuleView> severityRules() {
        return ruleRepo.findAll().stream()
                .sorted((a, b) -> a.getRankOrder().compareTo(b.getRankOrder()))
                .map(this::toView).toList();
    }

    
    @Transactional
    public SeverityRuleView updateSeverityRule(String severity, UpdateSeverityRuleRequest req,
                                               AuthUser admin, String ip) {
        SeverityRule r = ruleRepo.findById(severity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown severity"));
        if (req.escalateL1Pct() >= req.escalateL2Pct()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Level 1 percentage must be lower than level 2 percentage");
        }
        String before = "sla=" + r.getSlaHours() + "h l1=" + r.getEscalateL1Pct() + "% l2=" + r.getEscalateL2Pct() + "%";
        r.setSlaHours(req.slaHours());
        r.setEscalateL1Pct(req.escalateL1Pct());
        r.setEscalateL2Pct(req.escalateL2Pct());
        ruleRepo.save(r);
        audit.log(admin.userId(), "RULE_CHANGE", "SEVERITY_RULE", severity,
                before + " -> sla=" + req.slaHours() + "h l1=" + req.escalateL1Pct() + "% l2=" + req.escalateL2Pct() + "%", ip);
        return toView(r);
    }

    @Transactional(readOnly = true)
    public List<KeywordView> keywords() {
        return keywordRepo.findAll().stream()
                .map(k -> new KeywordView(k.getKeywordId(), k.getKeyword(), k.getSeverity(), k.isActive())).toList();
    }

    private SeverityRuleView toView(SeverityRule r) {
        return new SeverityRuleView(r.getSeverity(), r.getRankOrder(), r.getSlaHours(),
                r.getEscalateL1Pct(), r.getEscalateL2Pct(), r.getDescription());
    }
}
