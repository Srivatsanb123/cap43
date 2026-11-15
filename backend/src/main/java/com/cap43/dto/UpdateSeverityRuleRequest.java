package com.cap43.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateSeverityRuleRequest(
        @NotNull @Min(1) @Max(720) Integer slaHours,
        @NotNull @Min(1) @Max(100) Integer escalateL1Pct,
        @NotNull @Min(2) @Max(200) Integer escalateL2Pct) {}
