package com.fluxforged.worker_service.Services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Component
@Slf4j
public class PipelineConsumer {

    private final BuildExecutorService buildExecutorService;

    public PipelineConsumer(BuildExecutorService buildExecutorService) {
        this.buildExecutorService = buildExecutorService;
    }

    @KafkaListener(topics = "pipeline-events", groupId = "worker-group")
    public void listen(Map<String, Object> payload) {
        log.info("Received pipeline event: {}", payload);

        try {

            String storageKey = (String) payload.get("storageKey");
            String runId = (String) payload.get("runId");
            String userEmail = (String) payload.get("userEmail");
            String projectName = (String) payload.get("projectName");


            if (storageKey == null || runId == null) {
                log.error("Invalid pipeline event: missing storageKey or runId. Payload: {}", payload);
                return;
            }


            buildExecutorService.executePipeline(storageKey, runId, userEmail, projectName);

        } catch (Exception e) {
            log.error("Error processing pipeline event: {}", e.getMessage(), e);
        }
    }
}