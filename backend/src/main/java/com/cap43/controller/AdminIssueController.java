package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.AdminIssueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/issues")
@RequiredArgsConstructor
public class AdminIssueController {

    private final AdminIssueService service;

    @GetMapping
    public List<AdminIssueRow> list(@RequestParam(required = false) String status,
                                    @RequestParam(required = false) String severity,
                                    @RequestParam(defaultValue = "false") boolean overdue) {
        return service.list(status, severity, overdue);
    }

    @GetMapping("/{id}")
    public AdminIssueDetail detail(@PathVariable Integer id) {
        return service.detail(id);
    }

    @PatchMapping("/{id}/severity")
    public AdminIssueRow changeSeverity(@PathVariable Integer id, @Valid @RequestBody ChangeSeverityRequest req,
                                        @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.changeSeverity(id, req, admin, http.getRemoteAddr());
    }

    @PostMapping("/{id}/work-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderView assignVendor(@PathVariable Integer id, @Valid @RequestBody AssignVendorRequest req,
                                      @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.assignVendor(id, req, admin, http.getRemoteAddr());
    }
}
