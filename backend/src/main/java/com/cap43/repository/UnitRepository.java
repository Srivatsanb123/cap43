package com.cap43.repository;

import com.cap43.entity.Unit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Integer> {
    List<Unit> findByPropertyIdOrderByUnitNumberAsc(Integer propertyId);
    boolean existsByPropertyIdAndUnitNumber(Integer propertyId, String unitNumber);
}
