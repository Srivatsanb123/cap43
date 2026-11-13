package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnmaskRequest(@NotBlank @Size(min = 10, max = 400) String reason) {}
