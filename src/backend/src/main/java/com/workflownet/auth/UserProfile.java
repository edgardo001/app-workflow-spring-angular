package com.workflownet.auth;

import lombok.Data;
import java.util.List;

@Data
public class UserProfile {
    private String id;
    private String email;
    private String name;
    private List<String> roles;
}
