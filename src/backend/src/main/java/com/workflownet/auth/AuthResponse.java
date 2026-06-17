package com.workflownet.auth;

public record AuthResponse(String token, UserProfile user) {}
