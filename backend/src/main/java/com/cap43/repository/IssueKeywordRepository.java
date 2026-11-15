package com.cap43.repository;

import com.cap43.entity.IssueKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueKeywordRepository extends JpaRepository<IssueKeyword, Integer> {
    List<IssueKeyword> findByActiveTrue();
}
