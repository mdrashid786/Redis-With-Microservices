package com.notification.service.controller;

import com.notification.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// ================================================================
// CONTROLLER (for health check + manual testing)
// ================================================================

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    /**
     * Manual test endpoint - send email
     */
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        try {
            service.sendEmail(request.to, request.subject, request.body);
            return ResponseEntity.ok(new Response("Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Response("Error: " + e.getMessage()));
        }
    }

    /**
     * Manual test endpoint - send SMS
     */
    @PostMapping("/sms")
    public ResponseEntity<?> sendSMS(@RequestBody SMSRequest request) {
        try {
            service.sendSMS(request.phone, request.message);
            return ResponseEntity.ok(new Response("SMS sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new Response("Error: " + e.getMessage()));
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HealthResponse(
                "Notification Service",
                "UP",
                LocalDateTime.now().toString()
        ));
    }

    static class EmailRequest {
        public String to;
        public String subject;
        public String body;
    }

    static class SMSRequest {
        public String phone;
        public String message;
    }

    static class Response {
        public String message;
        public Response(String message) { this.message = message; }
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
