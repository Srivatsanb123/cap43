package com.cap43.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaseRow(Integer leaseId, String propertyName, String unitNumber, String tenantRef,
                       LocalDate startDate, LocalDate endDate, BigDecimal monthlyRent,
                       BigDecimal depositAmount, String status, LocalDate moveOutDate) {}
