package com.cap43.dto;

import java.time.LocalDateTime;

public record DisputeEventView(
        Integer eventId,
        Integer disputeId,
        Integer disputeItemId,
        String eventType,
        String actorType,
        Integer actorUserId,
        String detail,
        LocalDateTime createdAt
) {}

