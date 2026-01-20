package com.fluxforged.pipeline.service.Services;

import com.fluxforged.pipeline.service.PaymentClient;
import com.fluxforged.pipeline.service.PaymentRequiredException;
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
    private final PaymentClient paymentClient;

    public PipelineService(KafkaTemplate<String, Object> kafkaTemplate, StorageService storageService,PaymentClient paymentClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService;
        this.paymentClient=paymentClient;
    }

    public void startBuild(String email) {
        boolean isSubscribed = paymentClient.isUserSubscribed(email);

        if (!isSubscribed) {

            throw new RuntimeException("Please upgrade to Pro.");
        }

        System.out.println("Payment verified! Starting build for: " + email);
    }

    public void initiateFromZip(MultipartFile file, String email) {
        if (!paymentClient.isUserSubscribed(email)) {
            throw new PaymentRequiredException("Upgrade to Pro to run build pipelines.");
        }

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
        if (!paymentClient.isUserSubscribed(email)) {
            throw new PaymentRequiredException("Upgrade to Pro to run build pipelines.");
        }

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
