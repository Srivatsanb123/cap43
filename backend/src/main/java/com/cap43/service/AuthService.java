package com.cap43.service;

import com.cap43.dto.LoginRequest;
import com.cap43.dto.LoginResponse;
import com.cap43.dto.MeResponse;
import com.cap43.dto.RegisterRequest;
import com.cap43.entity.Tenant;
import com.cap43.entity.User;
import com.cap43.entity.Vendor;
import com.cap43.repository.TenantRepository;
import com.cap43.repository.UserRepository;
import com.cap43.repository.VendorRepository;
import com.cap43.security.AuthUser;
import com.cap43.security.JwtService;
import com.cap43.security.LoginAttemptService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "TENANT", "VENDOR");

    private final UserRepository userRepo;
    private final TenantRepository tenantRepo;
    private final VendorRepository vendorRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginAttemptService attempts;
    private final AuditService audit;
    private final String dummyHash;

    public AuthService(UserRepository userRepo, TenantRepository tenantRepo, VendorRepository vendorRepo,
                       PasswordEncoder encoder, JwtService jwt, LoginAttemptService attempts,
                       AuditService audit) {
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.vendorRepo = vendorRepo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.attempts = attempts;
        this.audit = audit;
        this.dummyHash = encoder.encode("not-a-real-password");
    }

    public LoginResponse login(LoginRequest req, String ip) {
        String key = req.email().trim().toLowerCase();

        if (attempts.isLocked(key)) {
            audit.log(null, "LOGIN_BLOCKED", null, null, "Too many failed attempts", ip);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Try again in a few minutes.");
        }

        Optional<User> found = userRepo.findByEmailIgnoreCase(key);
        boolean ok;
        if (found.isPresent()) {
            User u = found.get();
            ok = u.isEnabled() && encoder.matches(req.password(), u.getPasswordHash());
        } else {
            encoder.matches(req.password(), dummyHash);
            ok = false;
        }

        if (!ok) {
            attempts.recordFailure(key);
            audit.log(found.map(User::getUserId).orElse(null), "LOGIN_FAILED", null, null,
                    "Invalid credentials", ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = found.get();
        attempts.reset(key);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        Integer tenantId = tenantRepo.findByUserId(user.getUserId()).map(Tenant::getTenantId).orElse(null);
        Integer vendorId = vendorRepo.findByUserId(user.getUserId()).map(Vendor::getVendorId).orElse(null);

        audit.log(user.getUserId(), "LOGIN_SUCCESS", null, null, null, ip);
        return new LoginResponse(jwt.issue(user, tenantId, vendorId), user.getRole(),
                user.getFullName(), jwt.ttlMinutes());
    }

    @Transactional
    public LoginResponse register(RegisterRequest req, String ip) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email address already exists.");
        }

        String targetRole = req.role() != null ? req.role().trim().toUpperCase() : "TENANT";
        if (!ALLOWED_ROLES.contains(targetRole)) {
            targetRole = "TENANT";
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(targetRole);
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        user.setEnabled(true);
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepo.save(user);

        Integer tenantId = null;
        Integer vendorId = null;

        if ("TENANT".equals(targetRole)) {
            Tenant tenant = new Tenant();
            tenant.setUserId(savedUser.getUserId());
            tenant.setTenantRef(String.format("TN-%04d", savedUser.getUserId()));
            Tenant savedTenant = tenantRepo.save(tenant);
            tenantId = savedTenant.getTenantId();
        } else if ("VENDOR".equals(targetRole)) {
            Vendor vendor = new Vendor();
            vendor.setUserId(savedUser.getUserId());
            vendor.setCompanyName(req.companyName() != null && !req.companyName().isBlank()
                    ? req.companyName().trim()
                    : req.fullName().trim() + " Services");
            vendor.setTrade(req.trade() != null && !req.trade().isBlank() ? req.trade().trim().toUpperCase() : "GENERAL");
            vendor.setActive(true);
            Vendor savedVendor = vendorRepo.save(vendor);
            vendorId = savedVendor.getVendorId();
        }

        audit.log(savedUser.getUserId(), "REGISTER_SUCCESS", "User", String.valueOf(savedUser.getUserId()),
                "User registered as " + targetRole, ip);

        return new LoginResponse(jwt.issue(savedUser, tenantId, vendorId), savedUser.getRole(),
                savedUser.getFullName(), jwt.ttlMinutes());
    }

    public MeResponse me(AuthUser auth) {
        User u = userRepo.findById(auth.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        return new MeResponse(u.getUserId(), u.getRole(), u.getFullName(), auth.tenantId(), auth.vendorId());
    }
}
