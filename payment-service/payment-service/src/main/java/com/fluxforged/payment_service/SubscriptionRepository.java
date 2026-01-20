package com.fluxforged.payment_service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription,Integer> {
    Subscription findByUserEmail(String userEmail);
}
