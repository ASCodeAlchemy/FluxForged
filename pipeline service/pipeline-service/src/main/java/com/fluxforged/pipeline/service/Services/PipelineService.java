package com.fluxforged.pipeline.service.Services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PipelineService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StorageService storageService;

    public PipelineService(KafkaTemplate<String, String> kafkaTemplate, StorageService storageService) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService;
    }

    public void initiateFromGithub(String repoUrl) {
        String zipUrl = convertToZipUrl(repoUrl);
        String storageKey = storageService.saveFromUrl(zipUrl);

        triggerEvent(storageKey); // simplified
    }

    public void initiateFromZip(MultipartFile file) {
        String storageKey = storageService.saveFile(file);

        triggerEvent(storageKey); // simplified
    }

    private void triggerEvent(String storageKey) {
        // Generate a unique runId for this pipeline run
        String runId = java.util.UUID.randomUUID().toString();

        // Kafka message format: storageKey:runId
        String message = storageKey + ":" + runId;

        kafkaTemplate.send("pipeline-events", message);

        System.out.println("Kafka message sent: " + message);
    }

    private String convertToZipUrl(String repoUrl) {
        String apiBase = repoUrl.replace("https://github.com/", "https://api.github.com/repos/");
        return apiBase + "/zipball/main";
    }
}
