package com.cap43.repository;

import com.cap43.entity.Escalation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscalationRepository extends JpaRepository<Escalation, Integer> {
    List<Escalation> findByIssueIdOrderByLevelAsc(Integer issueId);
    boolean existsByIssueIdAndLevel(Integer issueId, Integer level);
    List<Escalation> findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();
    long countByAcknowledgedAtIsNull();
}
