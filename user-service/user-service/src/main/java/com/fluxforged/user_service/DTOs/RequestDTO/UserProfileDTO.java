package com.fluxforged.user_service.DTOs.RequestDTO;

import com.fluxforged.user_service.Enums.Roles;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileDTO {
    private UUID id;
    private UUID tenantId;
    private String fullName;
    private String username;
    private String email;
    private String bio;
    private Roles role;
    private Timestamp createdAt;

    public UserProfileDTO(UUID id, UUID tenantId, String fullName, String username, String email,String bio, Roles role, Timestamp createdAt) {
    }
}
