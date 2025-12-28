package com.fluxforged.user_service.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${spring.security.SECRET_KEY}")
    private String secretKey ;

    @Value("${spring.security.EXPIRATION}")
    private int Expiration;

    private SecretKey cachedKey;

    @PostConstruct
    public void init() {
        try {
            System.out.println("JWT Secret Key (Base64): " + secretKey);
            byte[] keybytes = Decoders.BASE64.decode(secretKey);
            cachedKey = Keys.hmacShaKeyFor(keybytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SECRET_KEY: must be a Base64-encoded string. Current value: " + secretKey, e);
        }
    }

    public SecretKey getKey() {
        return cachedKey;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String subject = userDetails.getUsername();
        if (userDetails instanceof com.fluxforged.user_service.Entity.Users user) {
            subject = user.getEmail();
        }
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuer("FluxForged")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Expiration))
                .and()
                .signWith(getKey())
                .compact();
    }

    public Claims extractALllClaims(String token){
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            System.err.println("JWT signature does not match: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("JWT extractALllClaims error: " + e.getMessage());
            return null;
        }
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimResolver){
        final Claims claims= extractALllClaims(token);
        return claims != null ? claimResolver.apply(claims) : null;
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName != null && userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        Date exp = extractExpiration(token);
        return exp != null && exp.before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    public String refreshToken(String token, UserDetails userDetails) {
        if (validateToken(token, userDetails)) {
            return generateToken(userDetails);
        }
        throw new IllegalArgumentException("Invalid or expired token");
    }
}
