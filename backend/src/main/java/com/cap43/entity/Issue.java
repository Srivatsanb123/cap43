package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueId;
    private Integer unitId;
    private Integer tenantId;
    private String category;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String severity;
    private String severitySource;
    private String status;
    private Integer escalationLevel;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime slaDueAt;
    private LocalDateTime resolvedAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
