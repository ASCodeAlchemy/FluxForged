package com.fluxforged.worker_service.Services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuildExecutorService {
    private final DockerClient dockerClient;
    private final MinioClient minioClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void executePipeline(String storageKey, String runId, String userEmail, String projectName) {
        boolean success = false;
        try {

            InputStream zipStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket("fluxforge-source").object(storageKey).build()
            );


            File workspace = new File(System.getProperty("java.io.tmpdir"), "fluxforge/" + runId);
            workspace.mkdirs();
            unzip(zipStream, workspace);


            File pomLocation = findPomXml(workspace);
            if (pomLocation == null) {
                throw new RuntimeException("No pom.xml found anywhere in the uploaded zip!");
            }

            File projectRoot = pomLocation.getParentFile();

            System.out.println("Project Root identified at: " + projectRoot.getAbsolutePath());


            String containerId = dockerClient.createContainerCmd("maven:3.9.5-eclipse-temurin-21")
                    .withHostConfig(HostConfig.newHostConfig()
                            // Mount the ACTUAL project root to /app
                            .withBinds(new Bind(projectRoot.getAbsolutePath(), new Volume("/app"))))
                    .withWorkingDir("/app")
                    .withCmd("mvn", "clean", "compile")
                    .exec()
                    .getId();




            dockerClient.startContainerCmd(containerId).exec();


            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame item) {
                            System.out.print(new String(item.getPayload()));
                        }
                    });


            int exitCode = dockerClient.waitContainerCmd(containerId)
                    .start()
                    .awaitStatusCode();

            success = (exitCode == 0);
            System.out.println("Build finished with exit code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {

            finishBuild(runId, userEmail, projectName, success);
        }
    }

    public void finishBuild(String runId, String userEmail, String projectName, boolean success) {
        Map<String, Object> result = new HashMap<>();
        result.put("runId", runId);
        result.put("userEmail", userEmail);
        result.put("projectName", projectName);
        result.put("status", success ? "SUCCESS" : "FAILED");
        result.put("logs", success ? "Build completed successfully." : "Build failed during compilation.");

        kafkaTemplate.send("build-results", runId, result);
    }

    private File findPomXml(File root) {
        if (root.getName().equals("pom.xml")) return root;
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    File found = findPomXml(file);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    private void unzip(InputStream is, File targetDir) throws IOException {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(is)) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

}
