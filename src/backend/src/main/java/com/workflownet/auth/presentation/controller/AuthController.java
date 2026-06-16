package com.workflownet.auth.presentation.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/profile")
    public void profile() {}
}
