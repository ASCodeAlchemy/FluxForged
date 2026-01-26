package com.fluxforged.user_service.Utils;

import com.fluxforged.user_service.Services.EmailService;
import com.fluxforged.user_service.Services.OTPService;
import com.fluxforged.user_service.Services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OTPService otpService;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public OAuth2LoginSuccessHandler(OTPService otpService, UserService userService, EmailService emailService) {
        this.otpService = otpService;
        this.userService = userService;
        this.emailService = emailService;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        if (userService.emailExists(email)) {
            // 1. Generate OTP
            String otp = otpService.generateOtp(email);

            // 2. Trigger Notification Service (Independent Route)
            try {
                emailService.sendVerificationEmail(email, otp, "LOGIN_OTP");
            } catch (Exception e) {
                System.err.println("❌ Failed to send OAuth OTP: " + e.getMessage());
            }

            // 3. Redirect user to your frontend OTP verification page
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/verify-otp?email=" + email);
        } else {
            // User not found in FluxForged DB
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/login?error=not_registered");
        }
    }
}
