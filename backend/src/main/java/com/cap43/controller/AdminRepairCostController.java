package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.RepairCostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/repair-costs")
@RequiredArgsConstructor
public class AdminRepairCostController {

    private final RepairCostService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CostEntryView create(@Valid @RequestBody CreateCostRequest req,
                                @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.create(req, admin, http.getRemoteAddr());
    }

    @GetMapping
    public LedgerView list(@RequestParam(required = false) Integer issueId,
                           @RequestParam(required = false) Integer workOrderId,
                           @RequestParam(required = false) Integer inspectionItemId) {
        return service.list(issueId, workOrderId, inspectionItemId);
    }

    @PostMapping("/{costId}/reverse")
    @ResponseStatus(HttpStatus.CREATED)
    public CostEntryView reverse(@PathVariable Integer costId, @Valid @RequestBody ReverseCostRequest req,
                                 @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.reverse(costId, req, admin, http.getRemoteAddr());
    }
}
