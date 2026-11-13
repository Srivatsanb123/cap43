package com.cap43.dto;

import java.time.LocalDateTime;

public record VendorWorkOrderView(Integer workOrderId, Integer issueId, String status,
                                  LocalDateTime assignedAt, String propertyName, String addressLine,
                                  String unitNumber, String title, String description, String severity,
                                  LocalDateTime slaDueAt, boolean overdue, String vendorNotes) {}
