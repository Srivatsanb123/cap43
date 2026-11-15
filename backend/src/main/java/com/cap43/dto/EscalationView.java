package com.cap43.dto;

import java.time.LocalDateTime;

public record EscalationView(Integer escalationId, Integer level, String reason,
                             LocalDateTime triggeredAt, LocalDateTime acknowledgedAt) {}
