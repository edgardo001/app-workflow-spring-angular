package com.workflownet.flow.presentation.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/stats")
    public void getStats() {}
}
