package com.cap43.repository;

import com.cap43.entity.IssueStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Integer> {
    List<IssueStatusHistory> findByIssueIdOrderByChangedAtAsc(Integer issueId);
}
