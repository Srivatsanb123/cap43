package com.cap43.repository;

import com.cap43.entity.RepairCost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairCostRepository extends JpaRepository<RepairCost, Integer> {
    List<RepairCost> findByIssueIdOrderByCostIdAsc(Integer issueId);
    List<RepairCost> findByWorkOrderIdOrderByCostIdAsc(Integer workOrderId);
    List<RepairCost> findByInspectionItemIdOrderByCostIdAsc(Integer inspectionItemId);
    boolean existsByReversesCostId(Integer reversesCostId);
}
