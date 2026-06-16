package com.workflownet.flow.presentation.controller;

import com.workflownet.flow.application.service.FlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flows")
@RequiredArgsConstructor
public class FlowController {
    private final FlowService flowService;
    
    @PostMapping
    public void create(@RequestBody Object request) {}
}
