package com.fluxforged.user_service.Controllers;

import com.fluxforged.user_service.Config.JWTService;
import com.fluxforged.user_service.Config.MyUserDetailService;
import com.fluxforged.user_service.DTOs.RequestDTO.*;
import com.fluxforged.user_service.DTOs.ResponseDTO.ResponseDTO;
import com.fluxforged.user_service.Services.EmailService;
import com.fluxforged.user_service.Services.OTPService;
import com.fluxforged.user_service.Services.PendingUserService;
import com.fluxforged.user_service.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController

public class UserController {

    private final UserService userService;
    private final JWTService jwtService;
    private final MyUserDetailService myUserDetailService;
    private final OTPService otpService;
    private final EmailService emailService;
    private final PendingUserService pendingUserService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public UserController(UserService userService, JWTService jwtService, MyUserDetailService myUserDetailService,
                          OTPService otpService, EmailService emailService, PendingUserService pendingUserService, KafkaTemplate kafkaTemplate) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.myUserDetailService = myUserDetailService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.pendingUserService = pendingUserService;
        this.kafkaTemplate=kafkaTemplate;

    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody UserDTO userDTO) throws Exception {
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (userService.emailExists(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Email already registered"));
        }
        pendingUserService.savePendingUser(userDTO);
        String otp = otpService.generateOtp(userDTO.getEmail());
        emailService.sendVerificationEmail(userDTO.getEmail(), otp, "REGISTER_OTP");

        return ResponseEntity.ok(new ResponseDTO("OTP sent to email for verification"));
    }




    @PostMapping("/verify-register-otp")
    public ResponseEntity<ResponseDTO> verifyRegisterOtp(@RequestBody OtpDTO otpDTO) throws Exception {
        String otp = otpDTO.getOtp();
        String email = otpService.getEmailByOtp(otp);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Invalid or expired OTP"));
        }

        UserDTO pendingUser = pendingUserService.getPendingUser(email);
        if (pendingUser == null) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Registration session expired"));
        }

        userService.signUp(pendingUser);
        pendingUserService.remove(email);
        otpService.removeOtp(otp);

        Map<String, Object> welcomeEvent = new HashMap<>();
        welcomeEvent.put("email", email);

        welcomeEvent.put("type", "REGISTER_SUCCESS");
        kafkaTemplate.send("auth-events", email, welcomeEvent);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO("User registered successfully"));
    }



    @PostMapping("/auth/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody UserDTO userDTO) throws Exception {
        if (!userService.emailExists(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Email not registered"));
        }

        String otp = otpService.generateOtp(userDTO.getEmail());
        emailService.sendVerificationEmail(userDTO.getEmail(), otp, "LOGIN_OTP");
        return ResponseEntity.ok(new ResponseDTO("OTP sent to email"));
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseDTO> verifyOtp(@RequestBody OtpDTO otpDTO, HttpServletResponse response) {
        String otp = otpDTO.getOtp();


        String email = otpService.verifyOtpAndGetEmail(otp);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Invalid or expired OTP"));
        }


        UserDetails userDetails = myUserDetailService.loadUserByUsername(email);
        String jwt = jwtService.generateToken(userDetails);

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        response.addCookie(cookie);

        return ResponseEntity.ok(new ResponseDTO("Login successful"));
    }


    @GetMapping("/my-profile")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        String email = userDetails.getUsername();
        UserProfileDTO profile = userService.getProfile(email);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<ResponseDTO> updateProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDTO userDTO) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        String email = userDetails.getUsername();
        ResponseDTO response = userService.updateProfile(email, userDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseDTO> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody ChangePasswordDTO changePasswordDTO) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userService.changePassword(userDetails.getUsername(), changePasswordDTO), HttpStatus.OK);
    }

    @PostMapping("/auth/resend-otp")
    public ResponseEntity<ResponseDTO> resendOtp(@RequestParam String email, @RequestParam String type) throws Exception {
        boolean isRegistration = "REGISTER_OTP".equalsIgnoreCase(type);

        if (isRegistration && pendingUserService.getPendingUser(email) == null) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No pending registration found for this email."));
        }

        if (!isRegistration && !userService.emailExists(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("Email not registered."));
        }

        String newOtp = otpService.generateOtp(email);
        emailService.sendVerificationEmail(email, newOtp, type.toUpperCase());

        return ResponseEntity.ok(new ResponseDTO("A new OTP has been sent to " + email));
    }
}
