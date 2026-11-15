package com.cap43.dto;

import jakarta.validation.constraints.NotNull;

public record ContestDisputeItemRequest(
        @NotNull String position, 
        String comment
) {}

