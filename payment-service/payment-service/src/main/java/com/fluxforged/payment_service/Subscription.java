package com.fluxforged.payment_service;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    private String razorpaySubscriptionId;

    @Enumerated(EnumType.STRING)
    private Memberships planType;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
