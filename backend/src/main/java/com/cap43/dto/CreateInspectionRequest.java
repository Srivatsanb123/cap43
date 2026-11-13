package com.cap43.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateInspectionRequest(
        @NotBlank @Pattern(regexp = "MOVE_IN|MOVE_OUT", message = "Must be MOVE_IN or MOVE_OUT")
        String inspectionType,
        @NotNull LocalDate inspectedOn,
        @Size(max = 2000) String notes) {}
