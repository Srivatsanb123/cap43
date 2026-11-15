package com.cap43.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "severity_rules")
public class SeverityRule {
    @Id
    private String severity;
    private Integer rankOrder;
    private Integer slaHours;
    @Column(name = "escalate_l1_pct")
    private Integer escalateL1Pct;
    @Column(name = "escalate_l2_pct")
    private Integer escalateL2Pct;
    private String description;
}
