package com.cap43.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "deposit_disputes")
public class DepositDispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer disputeId;
    private Integer leaseId;
    private Integer tenantId;
    private BigDecimal depositHeld;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime resolvedAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

