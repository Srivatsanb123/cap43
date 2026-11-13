package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseCostRequest(@NotBlank @Size(min = 10, max = 240) String reason) {}
