package com.cap43.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DisputeRow(
        Integer disputeId,
        Integer leaseId,
        Integer tenantId,
        String tenantRef,
        String propertyName,
        String unitNumber,
        BigDecimal depositHeld,
        String status,
        int itemCount,
        BigDecimal totalClaimed,
        LocalDateTime openedAt,
        LocalDateTime resolvedAt
) {}

