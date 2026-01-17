package com.fluxforged.user_service.DTOs.RequestDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthEvent {

    private String email;
    private String otp;
    private String type;
}
