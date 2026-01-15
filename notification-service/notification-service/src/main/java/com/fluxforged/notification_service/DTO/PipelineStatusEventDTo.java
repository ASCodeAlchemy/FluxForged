package com.fluxforged.notification_service.DTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineStatusEventDTo {
    private String userEmail;
    private String projectName;
    private String status;
    private String runId;
    private String logs;
}
