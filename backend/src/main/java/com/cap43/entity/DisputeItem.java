package com.cap43.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dispute_items")
public class DisputeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer disputeItemId;
    private Integer disputeId;
    private Integer moveOutItemId;
    private Integer moveInItemId;
    private BigDecimal claimedAmount;
    private String tenantPosition;
    private String tenantComment;
    private String systemClassification;
    private Integer appliedRuleId;
    @Column(columnDefinition = "JSON")
    private String ruleTrace;
    private String operatorDecision;
    private BigDecimal decidedAmount;
    private Integer decidedByUserId;
    private LocalDateTime decidedAt;
    private String decisionNotes;
    private LocalDateTime createdAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

