package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkOrderUpdateRequest(
        @NotBlank @Pattern(regexp = "ACCEPTED|REJECTED|IN_PROGRESS|COMPLETED", message = "Invalid status")
        String status,
        @Size(max = 500) String note) {}
