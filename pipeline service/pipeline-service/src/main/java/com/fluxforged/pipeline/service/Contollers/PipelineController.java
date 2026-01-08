package com.fluxforged.pipeline.service.Contollers;

import com.fluxforged.pipeline.service.Services.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    @Autowired
    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }


    @PostMapping("/fetch-github")
    public ResponseEntity<String> fetchFromGithub(@RequestParam String repoUrl) {
        pipelineService.initiateFromGithub(repoUrl);
        return ResponseEntity.ok("GitHub Pipeline Started");
    }


    @PostMapping(value = "/upload-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadZip(@RequestParam("file") MultipartFile file) {
        pipelineService.initiateFromZip(file);
        return ResponseEntity.ok("Zip Upload Pipeline Started");
    }

}
