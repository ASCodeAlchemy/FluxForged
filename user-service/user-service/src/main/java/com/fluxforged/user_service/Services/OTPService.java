package com.fluxforged.user_service.Services;

import com.fluxforged.user_service.DTOs.RequestDTO.AuthEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final int OTP_EXPIRATION_MINUTES = 10;


    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
        redisTemplate.opsForValue().set("otp:" + otp, email.toLowerCase(), Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        return otp;
    }


    public String getEmailByOtp(String otp) {
        return redisTemplate.opsForValue().get("otp:" + otp);
    }


    public boolean verifyOtp(String otp) {
        String email = getEmailByOtp(otp);
        if (email == null) return false;

        redisTemplate.delete("otp:" + otp);
        return true;
    }


    public String verifyOtpAndGetEmail(String otp) {
        String email = getEmailByOtp(otp);
        if (email == null) return null;

        // 1. Clear the OTP from Redis
        redisTemplate.delete("otp:" + otp);

        // 2. TRIGGER THE WELCOME EMAIL EVENT
        // We do this here because we know the verification is successful
        sendWelcomeEmailEvent(email);

        return email;
    }

    private void sendWelcomeEmailEvent(String email) {
        AuthEvent welcomeEvent = new AuthEvent();
        welcomeEvent.setEmail(email);
        welcomeEvent.setType("REGISTER_SUCCESS");

        kafkaTemplate.send("auth-events", email, welcomeEvent);
    }


    public void removeOtp(String otp) {
        redisTemplate.delete("otp:" + otp);
    }
}
