package com.workflowspring.auth;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String role;
}
