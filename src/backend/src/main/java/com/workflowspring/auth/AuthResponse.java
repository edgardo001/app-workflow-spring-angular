package com.workflowspring.auth;

public record AuthResponse(String token, UserProfile user) {}
