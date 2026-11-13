package com.cap43.repository;

import com.cap43.entity.DisputeItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeItemRepository extends JpaRepository<DisputeItem, Integer> {
    List<DisputeItem> findByDisputeIdOrderByDisputeItemIdAsc(Integer disputeId);
    List<DisputeItem> findByDisputeIdIn(Collection<Integer> disputeIds);
}

