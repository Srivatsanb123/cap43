package com.cap43.repository;

import com.cap43.entity.Lease;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseRepository extends JpaRepository<Lease, Integer> {
    Optional<Lease> findFirstByTenantIdAndStatusOrderByStartDateDesc(Integer tenantId, String status);
    List<Lease> findAllByOrderByStartDateDesc();
    List<Lease> findByStatusOrderByStartDateDesc(String status);
}
