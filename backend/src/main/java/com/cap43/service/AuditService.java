package com.cap43.service;

import com.cap43.entity.AuditLog;
import com.cap43.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Integer userId, String action, String entityType, String entityId,
                    String detail, String ip) {
        AuditLog a = new AuditLog();
        a.setUserId(userId);
        a.setAction(action);
        a.setEntityType(entityType);
        a.setEntityId(entityId);
        a.setDetail(detail);
        a.setIpAddress(ip);
        repo.save(a);
    }
}
