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

    @Value("${app.sync.python-path:python3}")
    private String pythonPath;

    @Value("${app.sync.script-path:sync/sync_pipeline.py}")
    private String scriptPath;

    public SyncTriggerResponse triggerSync(AuthUser admin, String ip) {
        log.info("S3 Data Sync triggered by user {} from {}", admin != null ? admin.userId() : "anonymous", ip);
        long start = System.currentTimeMillis();

        File scriptFile = resolveScriptFile(scriptPath);
        if (scriptFile == null || !scriptFile.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sync script not found at " + scriptPath);
        }

        String pythonExe = resolvePythonExecutable(pythonPath);

        try {
            ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptFile.getAbsolutePath());
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

            Integer adminId = admin != null ? admin.userId() : 1;

            if (exitCode != 0) {
                log.error("S3 Sync script failed with code {}: {}", exitCode, output);
                audit.log(adminId, "S3_SYNC_FAILED", "SYNC", "PIPELINE", "Exit code: " + exitCode, ip);
                return new SyncTriggerResponse("FAILED", "S3 sync execution failed: " + output, duration, details);
            }

            log.info("S3 Sync pipeline executed successfully in {} ms", duration);
            audit.log(adminId, "S3_SYNC_SUCCESS", "SYNC", "PIPELINE", "Duration: " + duration + "ms", ip);
            return new SyncTriggerResponse("SUCCESS", "S3 sync pipeline completed successfully. Delta CSVs staged to S3 for automated Snowpipe ingestion into Snowflake.", duration, details);

        } catch (Exception e) {
            log.error("Error executing S3 Sync script", e);
            Integer adminId = admin != null ? admin.userId() : 1;
            audit.log(adminId, "S3_SYNC_ERROR", "SYNC", "PIPELINE", e.getMessage(), ip);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 sync execution failed: " + e.getMessage());
        }
    }

    private File resolveScriptFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        File relativeParent = new File("..", path);
        if (relativeParent.exists()) {
            return relativeParent;
        }
        File inSync = new File("sync", "sync_pipeline.py");
        if (inSync.exists()) {
            return inSync;
        }
        File parentSync = new File("../sync", "sync_pipeline.py");
        if (parentSync.exists()) {
            return parentSync;
        }
        return file;
    }

    private String resolvePythonExecutable(String path) {
        if (path == null || path.isBlank()) {
            return "python3";
        }
        File file = new File(path);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        File relativeParent = new File("..", path);
        if (relativeParent.exists()) {
            return relativeParent.getAbsolutePath();
        }
        File syncVenvLinux = new File("sync/venv/bin/python");
        if (syncVenvLinux.exists()) {
            return syncVenvLinux.getAbsolutePath();
        }
        File parentVenvLinux = new File("../sync/venv/bin/python");
        if (parentVenvLinux.exists()) {
            return parentVenvLinux.getAbsolutePath();
        }
        File syncVenvWin = new File("sync/venv/Scripts/python.exe");
        if (syncVenvWin.exists()) {
            return syncVenvWin.getAbsolutePath();
        }
        File parentVenvWin = new File("../sync/venv/Scripts/python.exe");
        if (parentVenvWin.exists()) {
            return parentVenvWin.getAbsolutePath();
        }
        return path;
    }
}
