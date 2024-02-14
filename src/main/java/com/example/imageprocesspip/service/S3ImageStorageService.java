package com.example.imageprocesspip.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/*
@Service("s3ImageStorageService")
public class S3ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final String bucketName = "pip-captcha";

    public S3ImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void saveImage(String path, byte[] data) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
    }

    @Override
    public byte[] getImage(String path) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build();
        // This is a simplified example. In real scenario, you'd handle the stream properly.
        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }
}

 */