package com.cap43.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUnitRequest(
        @NotBlank @Size(max = 20) String unitNumber,
        @Min(0) @Max(200) Integer floorNo,
        @Min(0) @Max(20) Integer bedrooms) {}
