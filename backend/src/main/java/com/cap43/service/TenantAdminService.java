package com.cap43.service;

import com.cap43.common.PiiMasker;
import com.cap43.dto.TenantContact;
import com.cap43.dto.TenantSummary;
import com.cap43.entity.Tenant;
import com.cap43.entity.User;
import com.cap43.repository.TenantRepository;
import com.cap43.repository.UserRepository;
import com.cap43.security.AuthUser;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TenantAdminService {

    private final TenantRepository tenantRepo;
    private final UserRepository userRepo;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<TenantSummary> list() {
        List<Tenant> tenants = tenantRepo.findAll();
        Map<Integer, User> users = userRepo
                .findAllById(tenants.stream().map(Tenant::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getUserId, u -> u));

        return tenants.stream().map(t -> {
            User u = users.get(t.getUserId());
            return new TenantSummary(t.getTenantId(), t.getTenantRef(), u.getFullName(),
                    PiiMasker.maskEmail(u.getEmail()), PiiMasker.maskPhone(u.getPhone()));
        }).toList();
    }

    @Transactional(readOnly = true)
    public TenantContact unmask(Integer tenantId, String reason, AuthUser admin, String ip) {
        Tenant t = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
        User u = userRepo.findById(t.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        
        audit.log(admin.userId(), "PII_UNMASK", "TENANT", String.valueOf(tenantId), reason, ip);
        return new TenantContact(tenantId, u.getEmail(), u.getPhone());
    }
}
