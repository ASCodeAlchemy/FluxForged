package com.fluxforged.worker_service.Services;

import com.fluxforged.worker_service.Services.BuildExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PipelineEventListener {

    private final BuildExecutorService buildExecutorService;

    @KafkaListener(topics = "pipeline-events", groupId = "worker-group")
    public void handleTask(Map<String, Object> task) {
        String storageKey = (String) task.get("storageKey");
        String runId = (String) task.get("runId");
        String email = (String) task.get("userEmail");
        String project = (String) task.get("projectName");

        buildExecutorService.executePipeline(storageKey, runId, email, project);
    }
}
