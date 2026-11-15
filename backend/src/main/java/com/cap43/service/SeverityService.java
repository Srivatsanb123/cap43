package com.cap43.service;

import com.cap43.entity.IssueKeyword;
import com.cap43.entity.SeverityRule;
import com.cap43.repository.IssueKeywordRepository;
import com.cap43.repository.SeverityRuleRepository;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class SeverityService {

    public record Decision(String severity, String matchedKeyword) {}

    private static final String DEFAULT_SEVERITY = "MEDIUM";

    private final IssueKeywordRepository keywordRepo;
    private final SeverityRuleRepository ruleRepo;

    
    public Decision classify(String category, String title, String description) {
        String text = String.join(" ", nz(category), nz(title), nz(description));
        Map<String, Integer> rank = ruleRepo.findAll().stream()
                .collect(Collectors.toMap(SeverityRule::getSeverity, SeverityRule::getRankOrder));

        String best = null;
        String bestKeyword = null;
        for (IssueKeyword k : keywordRepo.findByActiveTrue()) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(k.getKeyword()), Pattern.CASE_INSENSITIVE);
            if (!p.matcher(text).find()) continue;
            Integer r = rank.get(k.getSeverity());
            if (r != null && (best == null || r < rank.get(best))) {
                best = k.getSeverity();
                bestKeyword = k.getKeyword();
            }
        }
        return best == null ? new Decision(DEFAULT_SEVERITY, null) : new Decision(best, bestKeyword);
    }

    public SeverityRule rule(String severity) {
        return ruleRepo.findById(severity).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown severity: " + severity));
    }

    public int slaHours(String severity) {
        return rule(severity).getSlaHours();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
