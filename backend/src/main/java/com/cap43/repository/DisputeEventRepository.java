package com.cap43.repository;

import com.cap43.entity.DisputeEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeEventRepository extends JpaRepository<DisputeEvent, Integer> {
    List<DisputeEvent> findByDisputeIdOrderByCreatedAtAsc(Integer disputeId);
}

