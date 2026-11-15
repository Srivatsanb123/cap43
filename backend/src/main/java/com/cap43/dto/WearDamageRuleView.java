package com.cap43.dto;

public record WearDamageRuleView(
        Integer ruleId,
        String ruleName,
        String itemCategory,
        Integer minAgeYears,
        Integer maxAgeYears,
        String moveInCondition,
        String moveOutCondition,
        String cause,
        String classification,
        Integer priority,
        boolean active,
        String notes
) {}

