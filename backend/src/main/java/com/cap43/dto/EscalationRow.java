package com.cap43.dto;

import java.time.LocalDateTime;

public record EscalationRow(Integer escalationId, Integer issueId, String issueTitle, String severity,
                            Integer level, String reason, LocalDateTime triggeredAt) {}
