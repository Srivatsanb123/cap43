package com.cap43.service;

import com.cap43.entity.IssueStatusHistory;
import com.cap43.repository.IssueStatusHistoryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class IssueHistoryService {

    private final IssueStatusHistoryRepository repo;

    public void record(Integer issueId, String oldStatus, String newStatus,
                       String oldSeverity, String newSeverity, Integer userId, String reason) {
        IssueStatusHistory h = new IssueStatusHistory();
        h.setIssueId(issueId);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setOldSeverity(oldSeverity);
        h.setNewSeverity(newSeverity);
        h.setChangedByUserId(userId);
        h.setChangeReason(reason == null ? null : reason.substring(0, Math.min(reason.length(), 255)));
        h.setChangedAt(LocalDateTime.now());
        repo.save(h);
    }
}
