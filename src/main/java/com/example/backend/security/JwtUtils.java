package com.example.backend.security;

import com.example.backend.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtils(
            @Value("${jwt.secret.key}") String secret,
            @Value("${jwt.expiration.ms:3600000}") long expirationMs) {

        // Enhanced key verification/decoding
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        if (decodedKey.length < 32) {
            throw new IllegalArgumentException("The JWT key must be exactly 256 bits (32 bytes) after Base64 decoding. Current length: " + decodedKey.length * 8 + " bits");
        }

        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256);

        // Add role-specific claims
        if (user instanceof com.example.backend.model.IndependentAccountant accountant) {
            builder.claim("firstName", accountant.getFirstName());
            builder.claim("lastName", accountant.getLastName());
        } else if (user instanceof com.example.backend.model.Company company) {
            builder.claim("companyName", company.getCompanyName());
        } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            // Add any specific claims for Admin here if needed
            builder.claim("admin", true);
        }

        return builder.compact();
    }



    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 604800000L)) // 7 days
                .signWith(secretKey)
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }



    // Change extractUsername() to:
    public String extractUsername(String token) {
        String username = extractClaims(token, Claims::getSubject);
        System.out.println("📧 JWT contains username: " + username); // Always log for debugging
        return username;
    }



    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(
                Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
        );
    }


    public boolean isTokenExpired(String token) {

        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    public Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("id", String.class);
    }




}