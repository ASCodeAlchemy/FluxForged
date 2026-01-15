package com.fluxforged.user_service.Services;

import com.fluxforged.user_service.DTOs.RequestDTO.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
public class PendingUserService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int PENDING_USER_TTL_MINUTES = 30;

    public PendingUserService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePendingUser(UserDTO userDTO) throws Exception {
        String key = "pendingUser:" + userDTO.getEmail().toLowerCase();
        String value = objectMapper.writeValueAsString(userDTO);
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(PENDING_USER_TTL_MINUTES));
    }

    public UserDTO getPendingUser(String email) throws Exception {
        String key = "pendingUser:" + email.toLowerCase();
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return objectMapper.readValue(value, UserDTO.class);
    }

    public void remove(String email) {
        String key = "pendingUser:" + email.toLowerCase();
        redisTemplate.delete(key);
    }
}
