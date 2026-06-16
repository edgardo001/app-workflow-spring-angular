package com.workflownet.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final SecretKey key;

    public JwtTokenService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateApprovalToken(String flowId, String email) {
        return Jwts.builder()
                .subject(flowId)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(86400)))
                .signWith(key)
                .compact();
    }

    public boolean validateApprovalToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        return validateApprovalToken(token);
    }

    public String getGithubIdFromToken(String token) {
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return claims.getPayload().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        List<String> roles = (List<String>) claims.getPayload().get("roles");
        return roles != null ? roles : List.of("USER");
    }
}
