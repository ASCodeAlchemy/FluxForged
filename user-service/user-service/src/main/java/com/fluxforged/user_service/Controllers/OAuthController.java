package com.fluxforged.user_service.Controllers;


import com.fluxforged.user_service.Config.JWTService;
import com.fluxforged.user_service.Config.MyUserDetailService;
import com.fluxforged.user_service.DTOs.ResponseDTO.ResponseDTO;
import com.fluxforged.user_service.Services.EmailService;
import com.fluxforged.user_service.Services.GoogleAuthService;
import com.fluxforged.user_service.Services.OTPService;
import com.fluxforged.user_service.Services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class OAuthController {


    private final UserService userService;
    private final OTPService otpService;
    private final GoogleAuthService googleAuthService;
    private final EmailService emailService;


    @Autowired
    public OAuthController(UserService userService, OTPService otpService, GoogleAuthService googleAuthService, EmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.googleAuthService = googleAuthService;
        this.emailService = emailService;
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> googleLogin(@RequestBody Map<String, String> payload) throws Exception {
        String idTokenString = payload.get("token");
        String email = googleAuthService.verifyToken(idTokenString);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Invalid Google Token"));
        }

        if (!userService.emailExists(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("User not registered in FluxForged"));
        }
        String otp = otpService.generateOtp(email);
        emailService.sendVerificationEmail(email, otp, "LOGIN_OTP");

        return ResponseEntity.ok(new ResponseDTO("Google Auth Verified. OTP sent to email."));
    }
}
