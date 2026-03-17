package com.payment.service.controller;

import com.payment.service.beans.Payment;
import com.payment.service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = service.processPayment(
                    request.orderId,
                    request.userId,
                    request.amount,
                    request.idempotencyKey
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new PaymentResponse("Payment processed", payment)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    static class PaymentRequest {
        public Long orderId;
        public Long userId;
        public BigDecimal amount;
        public String idempotencyKey;
    }

    static class PaymentResponse {
        public String message;
        public Payment payment;

        public PaymentResponse(String message, Payment payment) {
            this.message = message;
            this.payment = payment;
        }
    }

    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }
}
