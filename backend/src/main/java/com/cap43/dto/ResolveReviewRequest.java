package com.cap43.dto;

import jakarta.validation.constraints.NotNull;

public record ResolveReviewRequest(
        @NotNull String outcomeNotes
) {}

