package com.cap43.dto;

import java.time.LocalDateTime;

public record ReviewQueueRow(
        Integer reviewId,
        Integer disputeItemId,
        Integer disputeId,
        Integer issueId,
        String reasonCode,
        String reasonText,
        String status,
        Integer assignedToUserId,
        LocalDateTime createdAt,
        LocalDateTime decidedAt,
        String outcomeNotes
) {}

