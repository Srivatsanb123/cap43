package com.cap43.service;

import com.cap43.entity.Escalation;
import com.cap43.entity.Issue;
import com.cap43.entity.SeverityRule;
import com.cap43.repository.EscalationRepository;
import com.cap43.repository.IssueRepository;
import com.cap43.repository.SeverityRuleRepository;
import com.cap43.repository.SystemConfigRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


@Slf4j
@Service
public class EscalationService {

    private static final int DEFAULT_EMERGENCY_ACK_MINUTES = 15;

    private final IssueRepository issueRepo;
    private final EscalationRepository escalationRepo;
    private final SeverityRuleRepository ruleRepo;
    private final SystemConfigRepository configRepo;
    private final TransactionTemplate tx;

    public EscalationService(IssueRepository issueRepo, EscalationRepository escalationRepo,
                             SeverityRuleRepository ruleRepo, SystemConfigRepository configRepo,
                             PlatformTransactionManager txManager) {
        this.issueRepo = issueRepo;
        this.escalationRepo = escalationRepo;
        this.ruleRepo = ruleRepo;
        this.configRepo = configRepo;
        this.tx = new TransactionTemplate(txManager);
    }

    public int runCycle() {
        List<Integer> ids = issueRepo.findByStatusNot("RESOLVED").stream().map(Issue::getIssueId).toList();
        int created = 0;
        for (Integer id : ids) {
            try {
                Integer n = tx.execute(status -> escalateIfNeeded(id));   
                if (n != null) created += n;
            } catch (RuntimeException e) {
                log.warn("Escalation check failed for issue {}", id, e);
            }
        }
        return created;
    }

    private int escalateIfNeeded(Integer id) {
        Issue issue = issueRepo.findById(id).orElse(null);
        if (issue == null || "RESOLVED".equals(issue.getStatus())) return 0;
        SeverityRule rule = ruleRepo.findById(issue.getSeverity()).orElse(null);
        if (rule == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        long totalSec = Math.max(1, Duration.between(issue.getCreatedAt(), issue.getSlaDueAt()).getSeconds());
        long elapsedSec = Duration.between(issue.getCreatedAt(), now).getSeconds();
        double pct = elapsedSec * 100.0 / totalSec;

        int target = pct >= rule.getEscalateL2Pct() ? 2 : (pct >= rule.getEscalateL1Pct() ? 1 : 0);
        String level1Reason = rule.getEscalateL1Pct() + "% of SLA elapsed without resolution";

        int ackMinutes = emergencyAckMinutes();
        boolean emergencyUnacknowledged = "EMERGENCY".equals(issue.getSeverity())
                && issue.getAcknowledgedAt() == null
                && elapsedSec >= ackMinutes * 60L;
        if (emergencyUnacknowledged && target < 1) {
            target = 1;
            level1Reason = "Emergency not acknowledged within " + ackMinutes + " minutes";
        }

        int current = issue.getEscalationLevel() == null ? 0 : issue.getEscalationLevel();
        if (target <= current) return 0;

        int created = 0;
        for (int level = current + 1; level <= target; level++) {
            if (escalationRepo.existsByIssueIdAndLevel(id, level)) continue;
            Escalation e = new Escalation();
            e.setIssueId(id);
            e.setLevel(level);
            e.setReason(level == 1 ? level1Reason : "SLA breached");
            e.setTriggeredAt(now);
            escalationRepo.save(e);
            created++;
        }
        issue.setEscalationLevel(target);
        issueRepo.save(issue);
        return created;
    }

    private int emergencyAckMinutes() {
        try {
            return configRepo.findById("emergency.ack_minutes")
                    .map(c -> Integer.parseInt(c.getConfigValue().trim()))
                    .orElse(DEFAULT_EMERGENCY_ACK_MINUTES);
        } catch (NumberFormatException e) {
            return DEFAULT_EMERGENCY_ACK_MINUTES;
        }
    }
}
