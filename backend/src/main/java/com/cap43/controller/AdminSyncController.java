package com.cap43.controller;

import com.cap43.dto.SyncTriggerResponse;
import com.cap43.security.AuthUser;
import com.cap43.service.SyncTriggerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class AdminSyncController {

    private final SyncTriggerService syncService;

    @PostMapping("/run")
    public SyncTriggerResponse runSync(@AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return syncService.triggerSync(admin, http.getRemoteAddr());
    }
}

