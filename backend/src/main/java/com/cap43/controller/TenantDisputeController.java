package com.cap43.controller;

import com.cap43.dto.ContestDisputeItemRequest;
import com.cap43.dto.DisputeDetailView;
import com.cap43.dto.DisputeRow;
import com.cap43.security.AuthUser;
import com.cap43.service.DisputeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/disputes")
@RequiredArgsConstructor
public class TenantDisputeController {

    private final DisputeService service;

    @GetMapping
    public List<DisputeRow> list(@AuthenticationPrincipal AuthUser tenant) {
        return service.listForTenant(tenant);
    }

    @GetMapping("/{id}")
    public DisputeDetailView get(@PathVariable Integer id) {
        return service.getDispute(id);
    }

    @PostMapping("/{id}/items/{itemId}/contest")
    public DisputeDetailView contestItem(@PathVariable Integer id,
                                         @PathVariable Integer itemId,
                                         @Valid @RequestBody ContestDisputeItemRequest req,
                                         @AuthenticationPrincipal AuthUser tenant,
                                         HttpServletRequest http) {
        return service.contestItem(id, itemId, req, tenant, http.getRemoteAddr());
    }
}

