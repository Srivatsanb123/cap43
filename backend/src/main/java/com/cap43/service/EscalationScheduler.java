package com.cap43.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final EscalationService service;

    @Scheduled(fixedDelayString = "${app.escalation.interval-ms:60000}", initialDelay = 15000)
    public void run() {
        int n = service.runCycle();
        if (n > 0) log.info("Raised {} new escalation(s)", n);
    }
}
