package com.cap43.repository;

import com.cap43.entity.WorkOrder;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {
    List<WorkOrder> findByIssueIdOrderByAssignedAtAsc(Integer issueId);
    List<WorkOrder> findByIssueIdIn(Collection<Integer> issueIds);
    List<WorkOrder> findByVendorIdOrderByAssignedAtDesc(Integer vendorId);
    boolean existsByIssueIdAndStatusIn(Integer issueId, Collection<String> statuses);
}
