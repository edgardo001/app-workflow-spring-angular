package com.workflownet.auth;

import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    public String generateApprovalToken(String flowId, String email) {
        return "token";
    }
    public boolean validateApprovalToken(String token) {
        return true;
    }
}
