package com.fluxforged.user_service.Controllers;


import com.fluxforged.user_service.Config.JWTService;
import com.fluxforged.user_service.Config.MyUserDetailService;
import com.fluxforged.user_service.DTOs.RequestDTO.UserDTO;
import com.fluxforged.user_service.DTOs.ResponseDTO.ResponseDTO;
import com.fluxforged.user_service.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {


    private final UserService userService;
    private final JWTService jwtService;
    private final MyUserDetailService myUserDetailService;


    @Autowired
    public UserController(UserService userService, JWTService jwtService,MyUserDetailService myUserDetailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.myUserDetailService=myUserDetailService;
    }



    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> Register(@RequestBody UserDTO userDTO) {
        String pass = userDTO.getPassword();
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");

        }
        System.out.println(pass);
        return new ResponseEntity<>(userService.signUp(userDTO), HttpStatus.CREATED);
    }




    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody UserDTO userDto, HttpServletResponse response){
        ResponseDTO responseDto = userService.signIn(userDto);
        if ("Login Successful".equalsIgnoreCase(responseDto.getMessage())) {
            var userDetails = myUserDetailService.loadUserByUsername(userDto.getEmail());
            String jwt = jwtService.generateToken(userDetails);
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            response.addCookie(cookie);
            responseDto.setMessage("Login Successful");
        }
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
