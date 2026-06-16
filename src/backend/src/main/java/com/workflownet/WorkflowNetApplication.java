package com.workflownet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkflowNetApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowNetApplication.class, args);
    }
}
