package com.fluxforged.user_service.DTOs.RequestDTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID id;
    private String fullName;
    private String username;
    private String email;
    private String password;
    private String role;
    private String Status;
    private Timestamp createdAt;
}
