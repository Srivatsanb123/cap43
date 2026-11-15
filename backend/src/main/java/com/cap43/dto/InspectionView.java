package com.cap43.dto;

import java.time.LocalDate;
import java.util.List;

public record InspectionView(Integer inspectionId, Integer leaseId, String inspectionType,
                             LocalDate inspectedOn, String notes, List<InspectionItemView> items) {}
