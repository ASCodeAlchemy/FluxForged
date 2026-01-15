package com.fluxforged.pipeline.service.Services;

import com.fluxforged.pipeline.service.DTOs.PipelineStatusEventDTo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PipelineExecutionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
