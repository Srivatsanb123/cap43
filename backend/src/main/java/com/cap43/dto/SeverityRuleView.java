package com.cap43.dto;

public record SeverityRuleView(String severity, Integer rankOrder, Integer slaHours,
                               Integer escalateL1Pct, Integer escalateL2Pct, String description) {}
