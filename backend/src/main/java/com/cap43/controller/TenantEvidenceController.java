package com.cap43.controller;

import com.cap43.dto.EvidenceView;
import com.cap43.security.AuthUser;
import com.cap43.service.EvidenceService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantEvidenceController {

    private final EvidenceService service;

    @PostMapping(path = "/issues/{issueId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceView upload(@PathVariable Integer issueId, @RequestParam("file") MultipartFile file,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime capturedAt,
                               @AuthenticationPrincipal AuthUser tenant, HttpServletRequest http) {
        return service.uploadTenant(file, issueId, capturedAt, tenant, http.getRemoteAddr());
    }

    @GetMapping("/issues/{issueId}/evidence")
    public List<EvidenceView> list(@PathVariable Integer issueId, @AuthenticationPrincipal AuthUser tenant) {
        return service.listForIssueTenant(tenant, issueId);
    }

    @GetMapping("/evidence/{id}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable Integer id,
                                                       @AuthenticationPrincipal AuthUser tenant,
                                                       HttpServletRequest http) {
        return EvidenceResponses.of(service.load(id, tenant, http.getRemoteAddr()));
    }
}
