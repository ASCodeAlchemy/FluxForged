package com.fluxforged.notification_service;

import com.fluxforged.notification_service.DTO.AuthEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @KafkaListener(topics = {"pipeline-events", "build-results"}, groupId = "notification-group")
    public void consumePipelineUpdate(java.util.Map<String, Object> event) {
        System.out.println("Notification Service received event: " + event);

        String userEmail = (String) event.get("userEmail");
        String projectName = (String) event.get("projectName");
        String status = (String) event.get("status");
        String runId = (String) event.get("runId");
        String logs = (String) event.get("logs");

        String subject = "STARTED".equalsIgnoreCase(status) ?
                "🚀 FluxForged: Pipeline Started" :
                "✅ FluxForged: Build Success";

        String html = templateEngine.getPipelineEmailTemplate(
                projectName,
                status,
                runId,
                logs != null ? logs : "Processing..."
        );

        emailService.sendHtmlEmail(userEmail, subject, html);
        System.out.println("Email successfully sent to: " + userEmail);
    }

    @KafkaListener(topics = "auth-events", groupId = "notification-group")
    public void consumeAuthEvent(AuthEvent event) {
        String html = "";
        if ("REGISTER".equals(event.getType())) {
            html = templateEngine.getRegisterTemplate(event.getOtp());
        } else {
            html = templateEngine.getLoginTemplate(event.getOtp());
        }

        emailService.sendHtmlEmail(event.getEmail(), "FluxForged Security Code", html);
    }
}
