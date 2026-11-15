package com.cap43.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inspection_items")
public class InspectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;
    private Integer inspectionId;
    private String itemCategory;
    private String itemName;
    private String location;
    private String conditionRating;
    private Integer itemAgeYears;
    private String causeNoted;
    private String remarks;
}
