package com.fluxforged.pipeline.service.Services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PipelineService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StorageService storageService;

    public PipelineService(KafkaTemplate<String, Object> kafkaTemplate, StorageService storageService) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService;
    }

    public void initiateFromZip(MultipartFile file, String email) {
        try {
            String runId = UUID.randomUUID().toString();
            String storageKey = storageService.saveFile(file);

            sendKafkaTask(runId, storageKey, email, file.getOriginalFilename());
            System.out.println("Pipeline Zip Started: " + runId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate zip pipeline: " + e.getMessage());
        }
    }

    public void initiateFromGithub(String repoUrl, String email) {
        try {
            String runId = UUID.randomUUID().toString();
            String zipUrl = convertToZipUrl(repoUrl);
            String storageKey = storageService.saveFromUrl(zipUrl);

            sendKafkaTask(runId, storageKey, email, repoUrl);
            System.out.println("Pipeline GitHub Started: " + runId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate github pipeline: " + e.getMessage());
        }
    }

    private void sendKafkaTask(String runId, String storageKey, String email, String projectName) {
        Map<String, Object> task = new HashMap<>();
        task.put("runId", runId);
        task.put("storageKey", storageKey);
        task.put("userEmail", email);
        task.put("projectName", projectName);
        task.put("status", "STARTED");
        task.put("logs", "Initializing build environment...");

        kafkaTemplate.send("pipeline-events", runId, task);
    }

    private String convertToZipUrl(String repoUrl) {
        String apiBase = repoUrl.replace("https://github.com/", "https://api.github.com/repos/");
        return apiBase + "/zipball/main";
    }
}
