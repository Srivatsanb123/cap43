package com.cap43.repository;

import com.cap43.entity.EvidenceFile;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceFileRepository extends JpaRepository<EvidenceFile, Integer> {
    List<EvidenceFile> findByInspectionItemIdOrderByUploadedAtAsc(Integer inspectionItemId);
    List<EvidenceFile> findByIssueIdOrderByUploadedAtAsc(Integer issueId);
    List<EvidenceFile> findByInspectionItemIdIn(Collection<Integer> inspectionItemIds);
    boolean existsByInspectionItemIdAndSha256(Integer inspectionItemId, String sha256);
    boolean existsByIssueIdAndSha256(Integer issueId, String sha256);
    long countByInspectionItemId(Integer inspectionItemId);
    long countByIssueId(Integer issueId);
}
