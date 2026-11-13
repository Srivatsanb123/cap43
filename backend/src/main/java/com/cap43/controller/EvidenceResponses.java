package com.cap43.controller;

import com.cap43.service.EvidenceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class EvidenceResponses {

    private EvidenceResponses() {}

    
    static ResponseEntity<InputStreamResource> of(EvidenceService.Content c) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(c.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + c.fileName() + "\"")
                .body(new InputStreamResource(c.stream()));
    }
}
