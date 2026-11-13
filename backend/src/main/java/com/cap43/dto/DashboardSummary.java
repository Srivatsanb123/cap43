package com.cap43.dto;

import java.util.Map;

public record DashboardSummary(long total, long open, long overdue, long escalated,
                               long unacknowledgedEscalations, Map<String, Long> openBySeverity,
                               Map<String, Long> byStatus, Double slaCompliancePct) {}
