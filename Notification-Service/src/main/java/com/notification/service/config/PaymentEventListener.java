package com.notification.service.config;


import com.notification.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * Listen to payment.completed event
     */
    public void handlePaymentCompleted(String message) {
        System.out.println("📨 Event: payment.completed");
        System.out.println("   Message: " + message);

        try {
            notificationService.sendEmail(
                    "customer@example.com",
                    "Payment Confirmation",
                    "Your payment of Rs. 2000 has been processed successfully."
            );

        } catch (Exception e) {
            System.err.println("❌ Error sending notifications: " + e.getMessage());
        }
    }
}
