package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    // Pulls a static key from application.properties
    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private SecretKey getSigningKey() {
        // Decodes the base64 string into a byte array for HMAC-SHA
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        // Fix: Extracts only the String names (e.g., "ROLE_SUPER_ADMIN")
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("ROLES", roles) // Correct format: ["ROLE_SUPER_ADMIN"]
                .issuedAt(new Date(System.currentTimeMillis()))
                // 30 hours expiration
                .expiration(new Date(System.currentTimeMillis() + 30L * 60 * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
}