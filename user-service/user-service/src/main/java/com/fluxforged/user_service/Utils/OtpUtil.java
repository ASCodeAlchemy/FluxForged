package com.fluxforged.user_service.Utils;

public class OtpUtil {
    public static String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
