package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review_queue")
public class ReviewQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;
    private Integer disputeItemId;
    private Integer issueId;
    private String reasonCode;
    private String reasonText;
    private String status;
    private Integer assignedToUserId;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private Integer decidedByUserId;
    private String outcomeNotes;
}

