package com.cap43.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EndLeaseRequest(@NotNull LocalDate moveOutDate) {}
