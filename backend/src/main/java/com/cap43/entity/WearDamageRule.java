package com.cap43.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "wear_damage_rules")
public class WearDamageRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;
    private String ruleName;
    private String itemCategory;
    private Integer minAgeYears;
    private Integer maxAgeYears;
    private String moveInCondition;
    private String moveOutCondition;
    private String cause;
    private String classification;
    private Integer priority;
    private boolean active;
    private String notes;
}

