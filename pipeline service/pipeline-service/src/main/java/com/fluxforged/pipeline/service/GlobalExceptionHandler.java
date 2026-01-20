package com.fluxforged.pipeline.service;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<Map<String, String>> handlePaymentRequired(PaymentRequiredException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Payment Required");
        response.put("message", ex.getMessage());
        response.put("action", "Please visit /checkout to upgrade.");

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response); // HTTP 402
    }
}
