package com.cap43.dto;

import java.util.List;

public record TenantIssueDetail(TenantIssueView issue, List<TimelineEntry> timeline) {}
