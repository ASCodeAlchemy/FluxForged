package com.fluxforged.notification_service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

    import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final String GROUP_ID = "notification-v10-final";

    @KafkaListener(topics = {"pipeline-events", "build-results"}, groupId = GROUP_ID)
    public void consumePipelineUpdate(ConsumerRecord<String, Object> record) {
        Map<String, Object> event = normalizePayload(record.value());
        if (event == null || event.isEmpty()) {
            System.out.println("Received empty or unparseable pipeline event, skipping. raw=" + record.value());
            return;
        }

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

    @KafkaListener(topics = "auth-events", groupId = GROUP_ID)
    public void consumeAuthEvent(ConsumerRecord<String, Object> record) {
        Map<String, Object> event = normalizePayload(record.value());
        if (event == null || event.isEmpty()) return;

        String type = String.valueOf(event.get("type"));
        String email = String.valueOf(event.get("email"));
        String otp = String.valueOf(event.get("otp"));

        if ("REGISTER_OTP".equals(type)) {
            emailService.sendHtmlEmail(email, "Verify Your Account", templateEngine.getRegisterTemplate(otp));
        }
        else if ("LOGIN_OTP".equals(type)) {
            emailService.sendHtmlEmail(email, "Login Verification Code", templateEngine.getLoginTemplate(otp));
        }
        else if ("REGISTER_SUCCESS".equals(type)) {
            emailService.sendHtmlEmail(email, "Welcome to FluxForged!", templateEngine.getWelcomeTemplate(email));
            System.out.println("🎉 Final Welcome Email sent to: " + email);
        }
    }

    @KafkaListener(topics = "notification-events", groupId = GROUP_ID)
    public void handleNotification(ConsumerRecord<String, Object> record) {
        Map<String, Object> event = normalizePayload(record.value());
        if (event == null || event.isEmpty()) {
            System.out.println("Received empty or unparseable notification event, raw=" + record.value());
            return;
        }

        String type = String.valueOf(event.get("type"));
        String email = String.valueOf(event.get("email"));
        String plan = String.valueOf(event.getOrDefault("plan", "PRO"));

        if ("PAYMENT_SUCCESS".equals(type)) {
            try {
                String html;
                String subject;

                if ("ENTERPRISE".equalsIgnoreCase(plan)) {
                    html = templateEngine.getEnterpriseReceiptTemplate(email);
                    subject = "FluxForged: Welcome to Enterprise Tier! 💎";
                } else {
                    html = templateEngine.getPaymentReceiptTemplate(email);
                    subject = "FluxForged: Pro Plan Activated! 🚀";
                }

                emailService.sendHtmlEmail(email, subject, html);
                System.out.println("✅ " + plan + " receipt sent to: " + email);
            } catch (Exception e) {
                System.err.println("❌ Failed to send receipt: " + e.getMessage());
            }
        }
    }

    private Map<String, Object> normalizePayload(Object value) {
        if (value == null) return Collections.emptyMap();
        try {
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
            if (value instanceof String) {
                String s = (String) value;
                return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});
            }
            if (value instanceof byte[]) {
                String s = new String((byte[]) value, StandardCharsets.UTF_8);
                return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});
            }
            return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("Failed to normalize Kafka payload to Map: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private String loadTemplate(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/" + fileName);
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bdata, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not load email template: " + fileName, e);
        }
    }
}
