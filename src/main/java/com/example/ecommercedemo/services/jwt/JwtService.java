package com.example.ecommercedemo.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class JwtService {

    private final String secret =
            "my-super-secret-key-my-super-secret-key-my-super-secret-key-123";

    private final SecretKey key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );

    private static final long EXPIRATION_DATE = TimeUnit.DAYS.toMillis(7); // 7 days of timeout

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(username, claims);
    }

    private String createToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_DATE))
                .signWith(key)
                .compact();

    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        return getClaimsFromToken(token).getSubject().equals(userDetails.getUsername()) && !isTokenExpired(token);
    }




}
