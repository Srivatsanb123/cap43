package com.cap43.service;

import com.cap43.dto.EvidenceView;
import com.cap43.entity.EvidenceFile;
import com.cap43.repository.EvidenceFileRepository;
import com.cap43.repository.InspectionItemRepository;
import com.cap43.repository.IssueRepository;
import com.cap43.security.AuthUser;
import com.cap43.storage.EvidenceStorage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EvidenceService {

    public record Content(String fileName, String contentType, InputStream stream) {}

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final int MAX_PER_TARGET = 20;
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private final EvidenceFileRepository evidenceRepo;
    private final InspectionItemRepository itemRepo;
    private final IssueRepository issueRepo;
    private final EvidenceStorage storage;
    private final AuditService audit;

    

    @Transactional
    public EvidenceView uploadAdmin(MultipartFile file, Integer inspectionItemId, Integer issueId,
                                    LocalDateTime capturedAt, AuthUser user, String ip) {
        if ((inspectionItemId == null) == (issueId == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide exactly one of inspectionItemId or issueId");
        }
        if (inspectionItemId != null && !itemRepo.existsById(inspectionItemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection item not found");
        }
        if (issueId != null && !issueRepo.existsById(issueId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
        }
        return store(file, inspectionItemId, issueId, capturedAt, user, ip);
    }

    @Transactional
    public EvidenceView uploadTenant(MultipartFile file, Integer issueId, LocalDateTime capturedAt,
                                     AuthUser user, String ip) {
        requireOwnIssue(user, issueId);
        return store(file, null, issueId, capturedAt, user, ip);
    }

    private EvidenceView store(MultipartFile file, Integer itemId, Integer issueId,
                               LocalDateTime capturedAt, AuthUser user, String ip) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "File exceeds the 10 MB limit");
        }
        String type = file.getContentType();
        if (type == null || !TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only JPEG, PNG or PDF files are accepted");
        }
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the file");
        }
        if (!matchesMagic(type, data)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File content does not match its declared type");
        }

        long existing = itemId != null ? evidenceRepo.countByInspectionItemId(itemId)
                                       : evidenceRepo.countByIssueId(issueId);
        if (existing >= MAX_PER_TARGET) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Too many files are attached here already");
        }

        String sha = sha256(data);
        boolean duplicate = itemId != null ? evidenceRepo.existsByInspectionItemIdAndSha256(itemId, sha)
                                           : evidenceRepo.existsByIssueIdAndSha256(issueId, sha);
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This exact file is already attached");
        }

        String safe = safeName(file.getOriginalFilename());
        String target = itemId != null ? "inspection-items/" + itemId : "issues/" + issueId;
        String key = "evidence/" + target + "/" + UUID.randomUUID() + "-" + safe;
        storage.put(key, data, type);

        EvidenceFile e = new EvidenceFile();
        e.setInspectionItemId(itemId);
        e.setIssueId(issueId);
        e.setS3Key(key);
        e.setFileName(safe);
        e.setContentType(type);
        e.setSizeBytes((long) data.length);
        e.setSha256(sha);
        e.setCapturedAt(capturedAt);
        e.setUploadedByUserId(user.userId());
        e.setUploadedAt(LocalDateTime.now());
        EvidenceFile saved = evidenceRepo.save(e);

        audit.log(user.userId(), "EVIDENCE_UPLOAD", "EVIDENCE", String.valueOf(saved.getEvidenceId()),
                "target=" + target + " sha256=" + sha, ip);
        return view(saved);
    }

    

    @Transactional(readOnly = true)
    public List<EvidenceView> listForItem(Integer inspectionItemId) {
        return evidenceRepo.findByInspectionItemIdOrderByUploadedAtAsc(inspectionItemId)
                .stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<EvidenceView> listForIssueAdmin(Integer issueId) {
        return evidenceRepo.findByIssueIdOrderByUploadedAtAsc(issueId).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<EvidenceView> listForIssueTenant(AuthUser user, Integer issueId) {
        requireOwnIssue(user, issueId);
        return evidenceRepo.findByIssueIdOrderByUploadedAtAsc(issueId).stream().map(this::view).toList();
    }

    

    @Transactional(readOnly = true)
    public Content load(Integer evidenceId, AuthUser user, String ip) {
        EvidenceFile e = evidenceRepo.findById(evidenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence not found"));

        if ("TENANT".equals(user.role())) {
            boolean own = e.getIssueId() != null && user.tenantId() != null
                    && issueRepo.findById(e.getIssueId())
                        .map(i -> user.tenantId().equals(i.getTenantId())).orElse(false);
            if (!own) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence not found");
            }
        } else if (!"ADMIN".equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidence not found");
        }

        audit.log(user.userId(), "EVIDENCE_VIEW", "EVIDENCE", String.valueOf(evidenceId), null, ip);
        try {
            return new Content(e.getFileName(), e.getContentType(), storage.get(e.getS3Key()));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The file is not available in storage");
        }
    }

    

    private void requireOwnIssue(AuthUser user, Integer issueId) {
        Integer tenantId = user.tenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not linked to a tenant");
        }
        issueRepo.findById(issueId)
                .filter(i -> tenantId.equals(i.getTenantId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));
    }

    private EvidenceView view(EvidenceFile e) {
        return new EvidenceView(e.getEvidenceId(), e.getInspectionItemId(), e.getIssueId(), e.getDisputeItemId(),
                e.getFileName(), e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getCapturedAt(),
                e.getUploadedByUserId(), e.getUploadedAt());
    }

    private static boolean matchesMagic(String type, byte[] d) {
        if (d.length < 4) return false;
        return switch (type) {
            case "image/jpeg" -> (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF;
            case "image/png" -> (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G';
            case "application/pdf" -> d[0] == '%' && d[1] == 'P' && d[2] == 'D' && d[3] == 'F';
            default -> false;
        };
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String safeName(String original) {
        String n = original == null ? "file" : original;
        int slash = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
        if (slash >= 0) n = n.substring(slash + 1);
        n = n.replaceAll("[^A-Za-z0-9._-]", "_");
        if (n.isBlank() || n.chars().allMatch(c -> c == '.' || c == '_')) n = "file";
        return n.length() > 100 ? n.substring(n.length() - 100) : n;
    }
}
