package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "evidence_files")
public class EvidenceFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer evidenceId;
    private Integer inspectionItemId;
    private Integer issueId;
    private Integer disputeItemId;

    @Column(name = "s3_key")
    private String s3Key;

    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String sha256;
    private LocalDateTime capturedAt;
    private Integer uploadedByUserId;
    private LocalDateTime uploadedAt;
}
