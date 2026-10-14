package com.example.ecommercedemo.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Issues and validates the stateless JWTs used by the {@code /api/**} chain.
 *
 * <p>The signing secret is read from {@code app.jwt.secret} (env {@code APP_JWT_SECRET}) so
 * production deployments can supply a long random value without a code change. The built-in
 * default exists only so the demo starts out of the box — see README-HANDOFF.md.</p>
 */
@Service
public class JwtService {

    private static final long EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(7);

    @Value("${app.jwt.secret:my-super-secret-key-my-super-secret-key-my-super-secret-key-123}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return createToken(username, new HashMap<>());
    }

    private String createToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MILLIS))
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

    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }
}
