package com.cap43.service;

import com.cap43.dto.SyncTriggerResponse;
import com.cap43.security.AuthUser;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncTriggerService {

    private final AuditService audit;

    @Value("${app.sync.python-path:python}")
    private String pythonPath;

    @Value("${app.sync.script-path:sync/sync_pipeline.py}")
    private String scriptPath;

    public SyncTriggerResponse triggerSync(AuthUser admin, String ip) {
        log.info("S3 Data Sync triggered by user {} from {}", admin.userId(), ip);
        long start = System.currentTimeMillis();

        File pythonExe = new File(pythonPath);
        if (!pythonExe.exists()) {
            pythonPath = "python";
        }

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sync script not found at " + scriptPath);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
            pb.directory(scriptFile.getParentFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            long duration = System.currentTimeMillis() - start;

            Map<String, Object> details = new HashMap<>();
            details.put("exitCode", exitCode);
            details.put("output", output.toString());

            if (exitCode != 0) {
                log.error("S3 Sync script failed with code {}: {}", exitCode, output);
                audit.log(admin.userId(), "S3_SYNC_FAILED", "SYNC", "PIPELINE", "Exit code: " + exitCode, ip);
                return new SyncTriggerResponse("FAILED", "S3 sync execution failed: " + output, duration, details);
            }

            log.info("S3 Sync pipeline executed successfully in {} ms", duration);
            audit.log(admin.userId(), "S3_SYNC_SUCCESS", "SYNC", "PIPELINE", "Duration: " + duration + "ms", ip);
            return new SyncTriggerResponse("SUCCESS", "S3 sync pipeline completed successfully. Delta CSVs staged to S3 for automated Snowpipe ingestion into Snowflake.", duration, details);

        } catch (Exception e) {
            log.error("Error executing S3 Sync script", e);
            audit.log(admin.userId(), "S3_SYNC_ERROR", "SYNC", "PIPELINE", e.getMessage(), ip);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 sync execution failed: " + e.getMessage());
        }
    }
}

