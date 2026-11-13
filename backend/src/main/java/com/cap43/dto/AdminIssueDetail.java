package com.cap43.dto;

import java.util.List;

public record AdminIssueDetail(AdminIssueRow issue, String description, List<HistoryEntry> history,
                               List<WorkOrderView> workOrders, List<EscalationView> escalations) {}
