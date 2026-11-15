package com.cap43.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DisputeItemView(
        Integer disputeItemId,
        Integer disputeId,
        Integer moveOutItemId,
        String moveOutItemName,
        String moveOutCategory,
        String moveOutCondition,
        String moveOutCause,
        Integer moveInItemId,
        String moveInCondition,
        BigDecimal claimedAmount,
        String tenantPosition,
        String tenantComment,
        String systemClassification,
        Integer appliedRuleId,
        String appliedRuleName,
        String ruleTrace,
        String operatorDecision,
        BigDecimal decidedAmount,
        LocalDateTime decidedAt,
        String decisionNotes,
        long moveOutEvidenceCount,
        long moveInEvidenceCount
) {}

