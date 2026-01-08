package com.fluxforged.pipeline.service.Services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class PipelineService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StorageService storageService;


    public PipelineService(KafkaTemplate<String, Object> kafkaTemplate, StorageService storageService) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService;
    }
    private String convertToZipUrl(String repoUrl) {


        String apiBase = repoUrl.replace("https://github.com/", "https://api.github.com/repos/");


        return apiBase + "/zipball/main";
    }


    public void initiateFromGithub(String repoUrl) {

        String zipUrl = convertToZipUrl(repoUrl);


        String storageKey = storageService.saveFromUrl(zipUrl);

        triggerEvent(storageKey, "GITHUB");
    }

    public void initiateFromZip(MultipartFile file) {

        String storageKey = storageService.saveFile(file);

        triggerEvent(storageKey, "MANUAL_UPLOAD");
    }

    private void triggerEvent(String storageKey, String source) {
        Map<String, Object> event = new HashMap<>();
        event.put("storageKey", storageKey);
        event.put("sourceType", source);
        event.put("status", "QUEUED");

        kafkaTemplate.send("pipeline-events", event);
    }
}
