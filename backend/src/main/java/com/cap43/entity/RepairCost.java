package com.cap43.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repair_costs")
public class RepairCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer costId;
    private Integer issueId;
    private Integer workOrderId;
    private Integer inspectionItemId;
    private Integer vendorId;
    private String entryType;
    private BigDecimal amount;
    private String costType;
    private String description;
    private String invoiceRef;
    private Integer reversesCostId;
    private LocalDateTime incurredAt;
    private Integer enteredByUserId;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
