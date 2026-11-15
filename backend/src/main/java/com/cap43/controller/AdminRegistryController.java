package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.AdminRegistryService;
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
public class AdminRegistryController {

    private final AdminRegistryService service;

    @GetMapping("/properties")
    public List<PropertyView> properties() {
        return service.properties();
    }

    @PostMapping("/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyView createProperty(@Valid @RequestBody CreatePropertyRequest req,
                                       @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.createProperty(req, admin, http.getRemoteAddr());
    }

    @GetMapping("/properties/{propertyId}/units")
    public List<UnitView> units(@PathVariable Integer propertyId) {
        return service.units(propertyId);
    }

    @PostMapping("/properties/{propertyId}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public UnitView createUnit(@PathVariable Integer propertyId, @Valid @RequestBody CreateUnitRequest req,
                               @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.createUnit(propertyId, req, admin, http.getRemoteAddr());
    }

    @GetMapping("/leases")
    public List<LeaseRow> leases(@RequestParam(required = false) String status) {
        return service.leases(status);
    }

    @PostMapping("/leases/{leaseId}/end")
    public LeaseRow endLease(@PathVariable Integer leaseId, @Valid @RequestBody EndLeaseRequest req,
                             @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.endLease(leaseId, req, admin, http.getRemoteAddr());
    }
}
