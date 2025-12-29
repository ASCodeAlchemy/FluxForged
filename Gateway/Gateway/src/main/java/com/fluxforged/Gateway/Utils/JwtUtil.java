package com.fluxforged.Gateway.Utils;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;


@Component
public class JwtUtil {

    private final byte[] key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Base64.getDecoder().decode(secret);
    }

    public void validate(String token) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
