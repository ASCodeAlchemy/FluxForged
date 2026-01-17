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
                "✅ FluxForged: Build Status";

        String html = templateEngine.getPipelineEmailTemplate(
                projectName,
                status,
                runId,
                logs != null ? logs : "Processing..."
        );

        emailService.sendHtmlEmail(userEmail, subject, html);
        System.out.println("Email successfully sent to: " + userEmail);
    }
    @KafkaListener(topics = "auth-events", groupId = "notification-group-new")
    public void consumeAuthEvent(AuthEvent event) {
        if (event == null || event.getType() == null) {
            System.out.println("Received empty or invalid AuthEvent");
            return;
        }

        String html;
        String subject;

        switch (event.getType()) {
            case "REGISTER_OTP" -> {
                subject = "FluxForged: Verify your email";
                html = templateEngine.getRegisterTemplate(event.getOtp());
            }
            case "REGISTER_SUCCESS" -> {
                subject = "Welcome to FluxForged! 🚀";
                html = templateEngine.getWelcomeTemplate(event.getEmail());
            }
            default -> {
                subject = "FluxForged Security Code";
                html = templateEngine.getLoginTemplate(event.getOtp());
            }
        }

        emailService.sendHtmlEmail(event.getEmail(), subject, html);
        System.out.println("Email sent successfully: " + subject + " to " + event.getEmail());
    }
}
