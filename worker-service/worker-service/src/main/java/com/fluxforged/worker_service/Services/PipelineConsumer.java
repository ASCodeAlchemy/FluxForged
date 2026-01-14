package com.fluxforged.worker_service.Services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PipelineConsumer {

    private final BuildExecutorService buildExecutorService;

    public PipelineConsumer(BuildExecutorService buildExecutorService) {
        this.buildExecutorService = buildExecutorService;
    }

    @KafkaListener(topics = "pipeline-events", groupId = "worker-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);

        // Assuming message contains the storageKey and runId (e.g., JSON or "key:runId")
        String[] parts = message.split(":");
        String storageKey = parts[0];
        String runId = parts[1];

        buildExecutorService.executePipeline(storageKey, runId);
    }
}
