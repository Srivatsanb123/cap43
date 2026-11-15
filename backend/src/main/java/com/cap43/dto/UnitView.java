package com.cap43.dto;

public record UnitView(Integer unitId, Integer propertyId, String unitNumber, Integer floorNo,
                       Integer bedrooms, boolean occupied) {}
