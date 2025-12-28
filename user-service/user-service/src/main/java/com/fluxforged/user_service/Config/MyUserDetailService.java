package com.fluxforged.user_service.Config;

import com.fluxforged.user_service.Repositories.UserRepo;
import com.fluxforged.user_service.Entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

    @Autowired
    public MyUserDetailService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        // return a Spring Security User (implements UserDetails)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // password not present in entity; replace with actual password field when available
                Collections.emptyList()
        );
    }
}
