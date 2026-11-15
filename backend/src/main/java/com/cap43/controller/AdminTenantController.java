package com.cap43.controller;

import com.cap43.dto.TenantContact;
import com.cap43.dto.TenantSummary;
import com.cap43.dto.UnmaskRequest;
import com.cap43.security.AuthUser;
import com.cap43.service.TenantAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
public class AdminTenantController {

    private final TenantAdminService service;

    @GetMapping
    public List<TenantSummary> list() {
        return service.list();
    }

    @PostMapping("/{tenantId}/unmask")
    public TenantContact unmask(@PathVariable Integer tenantId,
                                @Valid @RequestBody UnmaskRequest req,
                                @AuthenticationPrincipal AuthUser admin,
                                HttpServletRequest http) {
        return service.unmask(tenantId, req.reason(), admin, http.getRemoteAddr());
    }
}
