package com.fluxforged.pipeline.service;

public class PaymentRequiredException extends RuntimeException{

    public PaymentRequiredException(String message) {
        super(message);
    }

    public PaymentRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
