package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "escalations")
public class Escalation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer escalationId;
    private Integer issueId;
    private Integer level;
    private String reason;
    private LocalDateTime triggeredAt;
    private Integer acknowledgedByUserId;
    private LocalDateTime acknowledgedAt;
}
