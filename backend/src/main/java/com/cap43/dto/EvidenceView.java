package com.cap43.dto;

import java.time.LocalDateTime;

public record EvidenceView(Integer evidenceId, Integer inspectionItemId, Integer issueId,
                           Integer disputeItemId, String fileName, String contentType, Long sizeBytes,
                           String sha256, LocalDateTime capturedAt, Integer uploadedByUserId,
                           LocalDateTime uploadedAt) {}
