package com.workflownet.auth;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public UserProfile getCurrentUser() {
        return new UserProfile();
    }
}
