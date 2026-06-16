package com.workflownet.shared.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
