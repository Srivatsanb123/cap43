package com.cap43.repository;

import com.cap43.entity.DepositDispute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositDisputeRepository extends JpaRepository<DepositDispute, Integer> {
    List<DepositDispute> findByTenantIdOrderByOpenedAtDesc(Integer tenantId);
    List<DepositDispute> findByLeaseIdOrderByOpenedAtDesc(Integer leaseId);
    List<DepositDispute> findAllByOrderByOpenedAtDesc();
    List<DepositDispute> findByStatusOrderByOpenedAtDesc(String status);
}

