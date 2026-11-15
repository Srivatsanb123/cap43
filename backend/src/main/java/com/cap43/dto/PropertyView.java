package com.cap43.dto;

public record PropertyView(Integer propertyId, String name, String addressLine, String city,
                           String state, String postalCode, long unitCount) {}
