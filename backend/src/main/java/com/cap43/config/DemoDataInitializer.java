package com.cap43.config;

import com.cap43.entity.User;
import com.cap43.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final String PLACEHOLDER = "PENDING_SET_BY_APP";

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Value("${app.demo.password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> pending = userRepo.findByPasswordHash(PLACEHOLDER);
        for (User u : pending) {
            u.setPasswordHash(encoder.encode(demoPassword));
        }
        userRepo.saveAll(pending);
        if (!pending.isEmpty()) {
            log.info("Set demo password for {} seeded users", pending.size());
        }
    }
}
