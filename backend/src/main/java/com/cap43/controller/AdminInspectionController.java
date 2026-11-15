package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.AdminInspectionService;
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
public class AdminInspectionController {

    private final AdminInspectionService service;

    @PostMapping("/leases/{leaseId}/inspections")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionView create(@PathVariable Integer leaseId, @Valid @RequestBody CreateInspectionRequest req,
                                 @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.create(leaseId, req, admin, http.getRemoteAddr());
    }

    @GetMapping("/leases/{leaseId}/inspections")
    public List<InspectionView> listForLease(@PathVariable Integer leaseId) {
        return service.listForLease(leaseId);
    }

    @GetMapping("/inspections/{inspectionId}")
    public InspectionView get(@PathVariable Integer inspectionId) {
        return service.get(inspectionId);
    }

    @PostMapping("/inspections/{inspectionId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionItemView addItem(@PathVariable Integer inspectionId, @Valid @RequestBody CreateItemRequest req,
                                      @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.addItem(inspectionId, req, admin, http.getRemoteAddr());
    }
}
