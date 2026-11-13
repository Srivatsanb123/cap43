package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 200) String addressLine,
        @Size(max = 60) String city,
        @Size(max = 60) String state,
        @Size(max = 12) String postalCode) {}
