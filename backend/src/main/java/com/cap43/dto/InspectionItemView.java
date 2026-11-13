package com.cap43.dto;

public record InspectionItemView(Integer itemId, Integer inspectionId, String itemCategory, String itemName,
                                 String location, String conditionRating, Integer itemAgeYears,
                                 String causeNoted, String remarks, long evidenceCount) {}
