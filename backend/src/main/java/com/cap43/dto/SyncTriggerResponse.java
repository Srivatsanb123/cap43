package com.cap43.dto;

import java.util.Map;

public record SyncTriggerResponse(
        String status,
        String message,
        long durationMs,
        Map<String, Object> details
) {}

