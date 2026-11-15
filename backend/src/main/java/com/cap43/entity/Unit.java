package com.cap43.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "units")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer unitId;
    private Integer propertyId;
    private String unitNumber;
    private Integer floorNo;
    private Integer bedrooms;
}
