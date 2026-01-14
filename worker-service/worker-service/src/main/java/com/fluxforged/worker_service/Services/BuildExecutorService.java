package com.fluxforged.worker_service.Services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class BuildExecutorService {
    private final DockerClient dockerClient;
    private final MinioClient minioClient;

    public BuildExecutorService(DockerClient dockerClient, MinioClient minioClient) {
        this.dockerClient = dockerClient;
        this.minioClient = minioClient;
    }

    public void executePipeline(String storageKey, String runId) {
        try {

            InputStream zipStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket("fluxforge-source").object(storageKey).build()
            );

            File workspace = new File("/tmp/builds/" + runId);
            workspace.mkdirs();
            unzip(zipStream, workspace);

            String containerId = dockerClient.createContainerCmd("maven:3.9.1-eclipse-temurin-17")
                    .withHostConfig(HostConfig.newHostConfig()
                            .withBinds(new Bind(workspace.getAbsolutePath(), new Volume("/app"))))
                    .withWorkingDir("/app")
                    .withCmd("mvn", "clean", "compile")
                    .exec()
                    .getId();

            dockerClient.startContainerCmd(containerId).exec();

            dockerClient.waitContainerCmd(containerId).start().awaitStatusCode();

            System.out.println("Build container finished: " + containerId);


            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new ResultCallback.Adapter<Frame>(){
                        @Override
                        public void onNext(Frame item) {
                            String logLine = new String(item.getPayload());
                            System.out.print(logLine);
                        }

                    }).awaitCompletion();




        } catch (Exception e) {
            e.printStackTrace();
        }
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
