package com.cap43.dto;

import java.time.LocalDateTime;

public record TenantIssueView(Integer issueId, String category, String title, String description,
                              String severity, String status, LocalDateTime createdAt,
                              LocalDateTime slaDueAt, LocalDateTime resolvedAt, boolean overdue) {}
