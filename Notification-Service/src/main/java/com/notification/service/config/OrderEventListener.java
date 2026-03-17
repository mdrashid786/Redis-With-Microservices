package com.notification.service.config;

import com.notification.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * Listen to order.created event
     */
    public void handleOrderCreated(String message) {
        System.out.println("📨 Event: order.created");
        System.out.println("   Message: " + message);

        try {
            // Send order confirmation email
            notificationService.sendEmail(
                    "customer@example.com",
                    "Order Confirmation",
                    "Your order has been created successfully. Order ID: ORD-123"
            );

            // Send SMS
            notificationService.sendSMS(
                    "+919876543210",
                    "Your order ORD-123 has been placed. Thank you!"
            );

        } catch (Exception e) {
            System.err.println("❌ Error sending notifications: " + e.getMessage());
        }
    }

    /**
     * Listen to order.status_changed event
     */
    public void handleOrderStatusChanged(String message) {
        System.out.println("📨 Event: order.status_changed");
        System.out.println("   Message: " + message);

        try {
            notificationService.sendEmail(
                    "customer@example.com",
                    "Order Status Update",
                    "Your order status has been updated: CONFIRMED"
            );

        } catch (Exception e) {
            System.err.println("❌ Error sending notifications: " + e.getMessage());
        }
    }
}
