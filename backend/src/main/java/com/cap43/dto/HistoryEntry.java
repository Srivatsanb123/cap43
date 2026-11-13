package com.cap43.dto;

import java.time.LocalDateTime;

public record HistoryEntry(LocalDateTime changedAt, String oldStatus, String newStatus,
                           String oldSeverity, String newSeverity, String reason,
                           Integer changedByUserId) {}
