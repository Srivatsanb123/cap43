package com.cap43.controller;

import com.cap43.dto.CreateIssueRequest;
import com.cap43.dto.TenantIssueDetail;
import com.cap43.dto.TenantIssueView;
import com.cap43.security.AuthUser;
import com.cap43.service.TenantIssueService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/issues")
@RequiredArgsConstructor
public class TenantIssueController {

    private final TenantIssueService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantIssueView create(@AuthenticationPrincipal AuthUser user,
                                  @Valid @RequestBody CreateIssueRequest req) {
        return service.create(user, req);
    }

    @GetMapping
    public List<TenantIssueView> list(@AuthenticationPrincipal AuthUser user) {
        return service.list(user);
    }

    @GetMapping("/{id}")
    public TenantIssueDetail get(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id) {
        return service.get(user, id);
    }
}
