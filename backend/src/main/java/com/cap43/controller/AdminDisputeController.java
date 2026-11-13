package com.cap43.controller;

import com.cap43.dto.*;
import com.cap43.security.AuthUser;
import com.cap43.service.DisputeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService service;

    @GetMapping
    public List<DisputeRow> list(@RequestParam(required = false) String status) {
        return service.listDisputes(status);
    }

    @GetMapping("/{id}")
    public DisputeDetailView get(@PathVariable Integer id) {
        return service.getDispute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisputeDetailView create(@Valid @RequestBody CreateDisputeClaimRequest req,
                                    @AuthenticationPrincipal AuthUser admin,
                                    HttpServletRequest http) {
        return service.createDispute(req, admin, http.getRemoteAddr());
    }

    @PostMapping("/{id}/items/{itemId}/decide")
    public DisputeDetailView decideItem(@PathVariable Integer id,
                                        @PathVariable Integer itemId,
                                        @Valid @RequestBody DecideDisputeItemRequest req,
                                        @AuthenticationPrincipal AuthUser admin,
                                        HttpServletRequest http) {
        return service.decideItem(id, itemId, req, admin, http.getRemoteAddr());
    }

    @GetMapping("/review-queue")
    public List<ReviewQueueRow> listReviewQueue(@RequestParam(required = false) String status) {
        return service.listReviewQueue(status);
    }

    @PostMapping("/review-queue/{id}/resolve")
    public ReviewQueueRow resolveReview(@PathVariable Integer id,
                                        @Valid @RequestBody ResolveReviewRequest req,
                                        @AuthenticationPrincipal AuthUser admin,
                                        HttpServletRequest http) {
        return service.resolveReview(id, req, admin, http.getRemoteAddr());
    }

    @GetMapping("/rules/wear-damage")
    public List<WearDamageRuleView> listWearDamageRules() {
        return service.listWearDamageRules();
    }
}

