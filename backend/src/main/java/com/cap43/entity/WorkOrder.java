package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "work_orders")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workOrderId;
    private Integer issueId;
    private Integer inspectionItemId;
    private Integer vendorId;
    private String status;
    private LocalDateTime assignedAt;
    private Integer assignedByUserId;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String vendorNotes;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
