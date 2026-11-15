package com.cap43.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateDisputeClaimRequest(
        @NotNull Integer leaseId,
        @NotNull List<ClaimItemRequest> items
) {
    public record ClaimItemRequest(
            @NotNull Integer moveOutItemId,
            Integer moveInItemId,
            @NotNull BigDecimal claimedAmount
    ) {}
}

