package com.cap43.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCostRequest(
        Integer issueId,
        Integer workOrderId,
        Integer inspectionItemId,
        Integer vendorId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "LABOR|MATERIAL|OTHER", message = "Invalid cost type") String costType,
        @Size(max = 255) String description,
        @Size(max = 40) String invoiceRef,
        LocalDateTime incurredAt) {}
