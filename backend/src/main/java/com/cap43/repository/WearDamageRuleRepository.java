package com.cap43.repository;

import com.cap43.entity.WearDamageRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WearDamageRuleRepository extends JpaRepository<WearDamageRule, Integer> {
    List<WearDamageRule> findByActiveTrueOrderByPriorityAsc();
    List<WearDamageRule> findByItemCategoryAndActiveTrueOrderByPriorityAsc(String itemCategory);
}

