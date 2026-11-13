package com.cap43.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;


@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record State(int failures, Instant lockedUntil) {}

    private final Map<String, State> states = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        State s = states.get(key);
        return s != null && s.lockedUntil() != null && Instant.now().isBefore(s.lockedUntil());
    }

    public void recordFailure(String key) {
        states.merge(key, new State(1, null), (old, fresh) -> {
            int failures = old.failures() + 1;
            return failures >= MAX_FAILURES
                    ? new State(0, Instant.now().plus(LOCK_TIME))
                    : new State(failures, null);
        });
    }

    public void reset(String key) {
        states.remove(key);
    }
}
