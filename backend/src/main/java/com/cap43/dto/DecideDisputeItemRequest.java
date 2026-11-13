package com.cap43.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DecideDisputeItemRequest(
        @NotNull String operatorDecision, 
        @NotNull @DecimalMin("0.00") BigDecimal decidedAmount,
        String decisionNotes
) {}

