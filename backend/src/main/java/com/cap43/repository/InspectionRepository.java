package com.cap43.repository;

import com.cap43.entity.Inspection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<Inspection, Integer> {
    List<Inspection> findByLeaseIdOrderByInspectedOnAsc(Integer leaseId);
    boolean existsByLeaseIdAndInspectionType(Integer leaseId, String inspectionType);
}
