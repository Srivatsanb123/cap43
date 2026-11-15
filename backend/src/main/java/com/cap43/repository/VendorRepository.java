package com.cap43.repository;

import com.cap43.entity.Vendor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Integer> {
    Optional<Vendor> findByUserId(Integer userId);
}
