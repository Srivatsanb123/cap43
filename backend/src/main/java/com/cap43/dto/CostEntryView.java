package com.cap43.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CostEntryView(Integer costId, Integer issueId, Integer workOrderId, Integer inspectionItemId,
                            Integer vendorId, String entryType, BigDecimal amount, String costType,
                            String description, String invoiceRef, Integer reversesCostId,
                            LocalDateTime incurredAt, Integer enteredByUserId) {}
