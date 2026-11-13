package com.cap43.dto;

import java.time.LocalDateTime;

public record WorkOrderView(Integer workOrderId, Integer vendorId, String vendorName, String status,
                            LocalDateTime assignedAt, LocalDateTime acceptedAt,
                            LocalDateTime startedAt, LocalDateTime completedAt, String vendorNotes) {}
