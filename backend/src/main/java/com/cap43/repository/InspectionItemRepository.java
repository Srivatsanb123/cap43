package com.cap43.repository;

import com.cap43.entity.InspectionItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, Integer> {
    List<InspectionItem> findByInspectionIdOrderByItemIdAsc(Integer inspectionId);
    List<InspectionItem> findByInspectionIdIn(Collection<Integer> inspectionIds);
}
