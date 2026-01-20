package com.fluxforged.pipeline.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", url = "${payment.service.url:http://localhost:8085}")
public interface PaymentClient {

    @GetMapping("/api/payments/is-subscribed")
    boolean isUserSubscribed(@RequestParam("email") String email);
}
