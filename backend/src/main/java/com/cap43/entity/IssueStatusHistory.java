package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;
    private Integer issueId;
    private String oldStatus;
    private String newStatus;
    private String oldSeverity;
    private String newSeverity;
    private Integer changedByUserId;
    private String changeReason;
    private LocalDateTime changedAt;
}
