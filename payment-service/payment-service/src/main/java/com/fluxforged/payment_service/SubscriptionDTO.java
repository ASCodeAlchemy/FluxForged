package com.fluxforged.payment_service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDTO {
    private String userEmail;

    private String razorpaySubscriptionId;

    private Memberships planType;

    private Status status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
