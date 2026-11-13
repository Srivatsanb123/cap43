package com.cap43.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DisputeDetailView(
        Integer disputeId,
        Integer leaseId,
        Integer tenantId,
        String tenantRef,
        String propertyName,
        String unitNumber,
        BigDecimal depositHeld,
        String status,
        LocalDateTime openedAt,
        LocalDateTime resolvedAt,
        List<DisputeItemView> items,
        List<DisputeEventView> events
) {}

