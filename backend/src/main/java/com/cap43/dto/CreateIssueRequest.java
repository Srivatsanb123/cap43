package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateIssueRequest(
        @NotBlank
        @Pattern(regexp = "PLUMBING|ELECTRICAL|HVAC|APPLIANCE|STRUCTURAL|GENERAL", message = "Invalid category")
        String category,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(min = 10, max = 2000) String description) {}
