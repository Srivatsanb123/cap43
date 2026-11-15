package com.cap43.repository;

import com.cap43.entity.Issue;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends JpaRepository<Issue, Integer> {

    List<Issue> findByTenantIdOrderByCreatedAtDesc(Integer tenantId);

    List<Issue> findByStatusNot(String status);

    long countByTenantIdAndCreatedAtAfter(Integer tenantId, LocalDateTime after);

    @Query("select i from Issue i where (:status is null or i.status = :status) "
            + "and (:severity is null or i.severity = :severity) order by i.createdAt desc")
    List<Issue> search(@Param("status") String status, @Param("severity") String severity);
}
