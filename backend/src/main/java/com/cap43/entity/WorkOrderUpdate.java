package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "work_order_updates")
public class WorkOrderUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer updateId;
    private Integer workOrderId;
    private String status;
    private String note;
    private Integer createdByUserId;
    private LocalDateTime createdAt;
}
