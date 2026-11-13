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

    public S3EvidenceStorage(@Value("${app.evidence.s3.bucket}") String bucket,
                             @Value("${app.evidence.s3.region}") String region) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("app.evidence.s3.bucket must be set");
        }
        this.bucket = bucket;
        this.s3 = S3Client.builder().region(Region.of(region)).build();
    }

    @Override
    public void put(String key, byte[] data, String contentType) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket).key(key).contentType(contentType)
                        .serverSideEncryption(ServerSideEncryption.AES256)
                        .build(),
                RequestBody.fromBytes(data));
    }

    @Override
    public InputStream get(String key) throws IOException {
        try {
            return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException(key);
        }
    }
}
