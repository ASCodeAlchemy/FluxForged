package com.fluxforged.worker_service.Config;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DockerConfig {
    @Bean
    public DockerClient dockerClient() {

        // 1. Create default config
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("npipe:////./pipe/docker_engine") // Windows named pipe
                .build();

        // 2. Create HTTP client
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        // 3. Create DockerClient instance
        return DockerClientImpl.getInstance(config, httpClient);
    }
}


//    @Bean
//    public MinioClient minioClient() {
//        String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
//        String accessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minio");
//        String secretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minio123");
//
//        return MinioClient.builder()
//                .endpoint(endpoint)
//                .credentials(accessKey, secretKey)
//                .build();
//    }

