package com.fluxforged.pipeline.service.Services;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BUCKET = "fluxforge-source";
    private static final long PART_SIZE = 10 * 1024 * 1024; // 10MB

    @Autowired
    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String saveFromUrl(String url) {
        String fileName = UUID.randomUUID() + "-github-repo.zip";

        return restTemplate.execute(url, HttpMethod.GET, null, response -> {
            try (InputStream inputStream = response.getBody()) {

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(BUCKET)
                                .object(fileName)
                                .stream(inputStream, -1, PART_SIZE)
                                .contentType("application/zip")
                                .build()
                );

                return fileName;

            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file to MinIO", e);
            }
        });
    }

    @Override
    public String saveFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), PART_SIZE)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload multipart file", e);
        }
    }

    @Override
    public String saveFromStream(InputStream inputStream, String fileName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .stream(inputStream, -1, PART_SIZE)
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store input stream", e);
        }
    }
}
