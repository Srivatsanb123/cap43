package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.AdminOpsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOpsController {

    private final AdminOpsService service;

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboard() {
        return service.dashboard();
    }

    @GetMapping("/escalations")
    public List<EscalationRow> escalations() {
        return service.openEscalations();
    }

    @PostMapping("/escalations/{id}/acknowledge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acknowledge(@PathVariable Integer id, @AuthenticationPrincipal AuthUser admin,
                            HttpServletRequest http) {
        service.acknowledgeEscalation(id, admin, http.getRemoteAddr());
    }

    @GetMapping("/vendors")
    public List<VendorOption> vendors() {
        return service.vendors();
    }

    @GetMapping("/rules/severity")
    public List<SeverityRuleView> severityRules() {
        return service.severityRules();
    }

    @PutMapping("/rules/severity/{severity}")
    public SeverityRuleView updateSeverityRule(@PathVariable String severity,
                                               @Valid @RequestBody UpdateSeverityRuleRequest req,
                                               @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.updateSeverityRule(severity, req, admin, http.getRemoteAddr());
    }

    @GetMapping("/rules/keywords")
    public List<KeywordView> keywords() {
        return service.keywords();
    }
}
