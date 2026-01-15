package com.fluxforged.pipeline.service.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PipelineStatusEventDTo {
    private String userEmail;
    private String projectName;
    private String status;
    private String runId;
    private String logs;
}
