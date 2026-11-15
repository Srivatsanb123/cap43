package com.cap43.security;

import com.cap43.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final NimbusJwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long ttlMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        SecretKey key = new SecretKeySpec(bytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.ttlMinutes = ttlMinutes;
    }

    public long ttlMinutes() {
        return ttlMinutes;
    }

    public String issue(User user, Integer tenantId, Integer vendorId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("cap43")
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .claim("role", user.getRole());
        if (tenantId != null) claims.claim("tenantId", tenantId);
        if (vendorId != null) claims.claim("vendorId", vendorId);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public Optional<AuthUser> parse(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            Integer userId = Integer.valueOf(jwt.getSubject());
            String role = jwt.getClaimAsString("role");
            Object t = jwt.getClaim("tenantId");
            Object v = jwt.getClaim("vendorId");
            return Optional.of(new AuthUser(
                    userId, role,
                    t == null ? null : ((Number) t).intValue(),
                    v == null ? null : ((Number) v).intValue()));
        } catch (RuntimeException e) {
            return Optional.empty();   
        }
    }
}
