package com.cap43.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignVendorRequest(@NotNull Integer vendorId, @Size(max = 500) String notes) {}
