package com.cap43.dto;

public record TenantSummary(Integer tenantId, String tenantRef, String fullName,
                            String maskedEmail, String maskedPhone) {}
