package com.cap43.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank
        @Pattern(regexp = "WALL_PAINT|CARPET|FLOORING|APPLIANCE|FIXTURE|COUNTERTOP|DOOR|WINDOW|OTHER",
                 message = "Invalid item category")
        String itemCategory,
        @NotBlank @Size(max = 80) String itemName,
        @Size(max = 60) String location,
        @NotBlank @Pattern(regexp = "NEW|GOOD|FAIR|POOR|DAMAGED|MISSING", message = "Invalid condition rating")
        String conditionRating,
        @Min(0) @Max(100) Integer itemAgeYears,
        @Pattern(regexp = "NORMAL_USE|TENANT_ACTION|UNKNOWN", message = "Invalid cause") String causeNoted,
        @Size(max = 500) String remarks) {}
