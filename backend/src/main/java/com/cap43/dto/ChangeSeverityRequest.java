package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangeSeverityRequest(
        @NotBlank @Pattern(regexp = "EMERGENCY|HIGH|MEDIUM|LOW", message = "Invalid severity") String severity,
        @NotBlank @Size(min = 10, max = 255) String reason) {}
