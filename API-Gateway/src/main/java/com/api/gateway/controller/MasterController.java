package com.api.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/gateway")
public class MasterController {


    @GetMapping("/health")
    public ResponseEntity<?> health() {
        System.out.println("Health check received at API Gateway");
        return ResponseEntity.ok(new HealthResponse(
                "Api Gateway Service",
                "UP",
                LocalDateTime.now().toString()
        ));
    }

    static class HealthResponse {
        public String service;
        public String status;
        public String timestamp;

        public HealthResponse(String service, String status, String timestamp) {
            this.service = service;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}
