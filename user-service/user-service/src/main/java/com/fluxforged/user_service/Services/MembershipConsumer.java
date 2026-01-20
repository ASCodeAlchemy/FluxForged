package com.fluxforged.user_service.Services;

import com.fluxforged.user_service.Entity.Users;
import com.fluxforged.user_service.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MembershipConsumer {

    private final UserRepo userRepository;

    @KafkaListener(topics = "membership-updates", groupId = "user-service-group")
    public void consumeMembershipUpdate(Map<String, Object> event) {
        System.out.println("📥 User-Service received event: " + event);

        try {
            String email = String.valueOf(event.get("email"));
            String plan = String.valueOf(event.get("plan"));


            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            user.setCurrentMembership(plan);

            userRepository.save(user);

            System.out.println("✅ Database updated: " + email + " is now " + plan);

        } catch (Exception e) {
            System.err.println("❌ User Database update failed: " + e.getMessage());
        }
    }
}
