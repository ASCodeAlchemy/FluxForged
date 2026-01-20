package com.fluxforged.user_service.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public EmailService(KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVerificationEmail(String toEmail, String code, String type) {

        Map<String, String> authEvent = new HashMap<>();
        authEvent.put("email", toEmail);
        authEvent.put("otp", code);
        authEvent.put("type", type);


        kafkaTemplate.send("auth-events", authEvent);

        System.out.println("📤 Kafka event sent for: " + type);

    }
}
