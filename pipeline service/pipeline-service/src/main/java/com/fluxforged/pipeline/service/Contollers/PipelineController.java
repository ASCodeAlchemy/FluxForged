package com.fluxforged.pipeline.service.Contollers;

import com.fluxforged.pipeline.service.Services.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PipelineController {

    private final PipelineService pipelineService;

    @Autowired
    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/fetch-github")
    public ResponseEntity<String> fetchFromGithub(
            @RequestHeader("X-User-Email") String email,
            @RequestParam String repoUrl
    ) {
        try {
            pipelineService.initiateFromGithub(repoUrl, email);
            return ResponseEntity.ok("GitHub Pipeline Started for " + email);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @PostMapping(value = "/upload-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadZip(
            @RequestHeader("X-User-Email") String email,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            pipelineService.initiateFromZip(file, email);
            return ResponseEntity.ok("Zip Upload Pipeline Started for " + email);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    private ResponseEntity<String> handleException(Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null) root = root.getCause();
        if (root instanceof java.net.ConnectException) {
            return ResponseEntity.status(503).body("Infrastructure unreachable: " + root.getMessage());
        }
        return ResponseEntity.status(500).body("Internal Error: " + root.getMessage());
    }
}

