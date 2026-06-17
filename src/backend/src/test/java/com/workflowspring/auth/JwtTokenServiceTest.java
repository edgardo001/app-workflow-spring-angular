package com.workflowspring.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private final JwtTokenService tokenService = new JwtTokenService(
            "my-super-secret-key-that-is-at-least-256-bits-long-for-testing");

    @Test
    void shouldGenerateToken() {
        String token = tokenService.generateApprovalToken("flow-123", "alice@test.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldValidateToken() {
        String token = tokenService.generateApprovalToken("flow-123", "alice@test.com");
        assertTrue(tokenService.validateApprovalToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(tokenService.validateApprovalToken("invalid-token"));
    }
}
