package com.fluxforged.user_service.DTOs.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MembershipUpdateEvent {
    private String email;
    private String planType;
    private String status;
}
