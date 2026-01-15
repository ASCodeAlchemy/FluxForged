package com.fluxforged.user_service.Services;

import com.fluxforged.user_service.DTOs.RequestDTO.ChangePasswordDTO;
import com.fluxforged.user_service.DTOs.RequestDTO.UserDTO;
import com.fluxforged.user_service.DTOs.RequestDTO.UserProfileDTO;
import com.fluxforged.user_service.DTOs.ResponseDTO.ResponseDTO;
import com.fluxforged.user_service.Entity.Users;
import com.fluxforged.user_service.Repositories.UserRepo;
import com.fluxforged.user_service.Enums.Roles;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;



    @Autowired
    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;

    }


public Users register(UserDTO userDTO){
if(userDTO.getPassword()==null || userDTO.getPassword().isEmpty()){
throw new IllegalArgumentException("Password cannot be null");
}

Users user = new Users();

user.setFullName(userDTO.getFullName());
user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
user.setEmail(userDTO.getEmail());
    user.setRole(Roles.valueOf(userDTO.getRole().toUpperCase()));

user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
user.setUsername(userDTO.getUsername());

return user;

}

    public ResponseDTO signUp(UserDTO userDTO) {
        Optional<Users> userEmail = userRepo.findByEmail(userDTO.getEmail());
        if (userEmail.isPresent()) {
            throw new IllegalStateException("Email is Already Registered");

        }
        Users user = register(userDTO);
        userRepo.save(user);
        ResponseDTO dto = new ResponseDTO();
        dto.setMessage("User Registered Successfully");
        return dto;

    }


    public ResponseDTO signIn(UserDTO userDTO) {
        Users user = userRepo.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Email"));

        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid Password");
        }

        return new ResponseDTO("Password verified. OTP sent.");
    }


    public UserProfileDTO getProfile(String email) {

        Users user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found currently logged in"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
    public ResponseDTO updateProfile(String email, UserDTO updatedData) {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found currently logged in."));

        if (updatedData.getFullName() != null && !updatedData.getFullName().isEmpty()) {
            user.setFullName(updatedData.getFullName());
        }
        if (updatedData.getBio() != null) {
            user.setBio(updatedData.getBio());
        }
        userRepo.save(user);
        return new ResponseDTO("Profile updated successfully");
    }
    @Transactional
    public ResponseDTO changePassword(String email, ChangePasswordDTO passwordDTO) {
        Users user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found currently logged in."));
        if(!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword()))
        {
            throw new IllegalArgumentException("Incorrect password.");
        }
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        return new ResponseDTO("Password changed successfully");
    }

    public boolean emailExists(String email) {
        return userRepo.findByEmail(email).isPresent();
    }



}
