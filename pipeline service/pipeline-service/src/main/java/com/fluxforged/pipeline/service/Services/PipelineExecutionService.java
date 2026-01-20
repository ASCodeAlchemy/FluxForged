package com.fluxforged.pipeline.service.Services;

import com.fluxforged.pipeline.service.DTOs.PipelineStatusEventDTo;
import com.fluxforged.pipeline.service.PaymentClient;
import com.fluxforged.pipeline.service.PaymentRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PipelineExecutionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentClient paymentClient;
    private final StorageService storageService;

    public void processPipelineRequest(MultipartFile file, String email, String projectName) {

        boolean isPro = paymentClient.isUserSubscribed(email);

        if (!isPro) {
            throw new PaymentRequiredException("Upgrade to Pro to run build pipelines.");
        }


        String storageKey = storageService.saveFile(file);
        String runId = UUID.randomUUID().toString();

        sendKafkaTask(runId, storageKey, email, projectName);
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

    public void triggerNotification(String email, String project, String status, String runId, String logs) {
        PipelineStatusEventDTo event = new PipelineStatusEventDTo(
                email,
                project,
                status,
                runId,
                logs
        );


        kafkaTemplate.send("pipeline-updates", runId, event);
    }
}
