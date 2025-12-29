package com.fluxforged.Gateway.Utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;


@Component
public class JwtUtil {
    private static final String SECRET =
            "SUPER_SECRET_KEY_256_BITS_LONG_CHANGE_ME";

    public void validate(String token) {
        Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token);
    }
}
