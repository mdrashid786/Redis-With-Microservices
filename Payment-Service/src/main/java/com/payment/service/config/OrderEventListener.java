package com.payment.service.config;

import com.payment.service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


// ================================================================
// PUB/SUB LISTENER
// ================================================================
@Component
class OrderEventListener {

    @Autowired
    private PaymentService paymentService;

    /**
     * Listen to order.created events
     */
    public void handleOrderCreated(String message) {
        System.out.println("📨 Received order.created event: " + message);

        // Parse message: ORDER_CREATED:{orderId:5,userId:1,productId:10}
        try {
            // Extract orderId, userId from message
            Long orderId = extractLongFromMessage(message, "orderId");
            Long userId = extractLongFromMessage(message, "userId");
            BigDecimal amount = BigDecimal.valueOf(100); // Mock amount

            // Process payment
            String idempotencyKey = "order:" + orderId;
            paymentService.processPayment(orderId, userId, amount, idempotencyKey);

            System.out.println("✅ Payment processed for order: " + orderId);

        } catch (Exception e) {
            System.err.println("❌ Error processing payment: " + e.getMessage());
        }
    }

    private Long extractLongFromMessage(String message, String field) {
        String pattern = field + ":(\\d+)";
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(pattern).matcher(message);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}
