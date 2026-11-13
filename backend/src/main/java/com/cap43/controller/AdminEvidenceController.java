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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/evidence")
@RequiredArgsConstructor
public class AdminEvidenceController {

    private final EvidenceService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceView upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) Integer inspectionItemId,
                               @RequestParam(required = false) Integer issueId,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime capturedAt,
                               @AuthenticationPrincipal AuthUser admin, HttpServletRequest http) {
        return service.uploadAdmin(file, inspectionItemId, issueId, capturedAt, admin, http.getRemoteAddr());
    }

    @GetMapping
    public List<EvidenceView> list(@RequestParam(required = false) Integer inspectionItemId,
                                   @RequestParam(required = false) Integer issueId) {
        if ((inspectionItemId == null) == (issueId == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide exactly one of inspectionItemId or issueId");
        }
        return inspectionItemId != null ? service.listForItem(inspectionItemId)
                                        : service.listForIssueAdmin(issueId);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable Integer id,
                                                       @AuthenticationPrincipal AuthUser admin,
                                                       HttpServletRequest http) {
        return EvidenceResponses.of(service.load(id, admin, http.getRemoteAddr()));
    }
}
