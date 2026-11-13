package com.cap43.repository;

import com.cap43.entity.ReviewQueue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewQueueRepository extends JpaRepository<ReviewQueue, Integer> {
    List<ReviewQueue> findByStatusOrderByCreatedAtDesc(String status);
    List<ReviewQueue> findAllByOrderByCreatedAtDesc();
    Optional<ReviewQueue> findByDisputeItemId(Integer disputeItemId);
    Optional<ReviewQueue> findByIssueId(Integer issueId);
}

