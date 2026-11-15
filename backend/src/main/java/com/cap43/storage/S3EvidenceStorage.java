package com.cap43.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

@Component
public class S3EvidenceStorage implements EvidenceStorage {

    private final S3Client s3;
    private final String bucket;

    public S3EvidenceStorage(@Value("${app.evidence.s3.bucket:propledger}") String bucket,
            @Value("${app.evidence.s3.region:ap-south-2}") String region) {
        this.bucket = (bucket != null) ? bucket.trim() : "";
        String reg = (region != null && !region.isBlank()) ? region.trim() : "ap-south-2";
        S3Client client = null;
        try {
            client = S3Client.builder().region(Region.of(reg)).build();
        } catch (Exception e) {
            client = null;
        }
        this.s3 = client;
    }

    @Override
    public void put(String key, byte[] data, String contentType) {
        if (s3 == null || bucket.isBlank()) {
            return;
        }
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build(),
                RequestBody.fromBytes(data));
    }

    @Override
    public InputStream get(String key) throws IOException {
        if (s3 == null || bucket.isBlank()) {
            throw new FileNotFoundException(key);
        }
        try {
            return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException(key);
        }
    }
}
