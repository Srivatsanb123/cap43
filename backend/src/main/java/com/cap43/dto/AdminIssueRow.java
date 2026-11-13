package com.cap43.dto;

import java.time.LocalDateTime;

public record AdminIssueRow(Integer issueId, String propertyName, String unitNumber, String tenantRef,
                            String category, String title, String severity, String severitySource,
                            String status, Integer escalationLevel, LocalDateTime createdAt,
                            LocalDateTime slaDueAt, LocalDateTime resolvedAt, boolean overdue,
                            String vendorName) {}
