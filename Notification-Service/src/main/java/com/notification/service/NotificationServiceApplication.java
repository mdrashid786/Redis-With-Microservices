package com.notification.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🔥 NOTIFICATION SERVICE (Port 8004)
 *
 * Stateless event-driven microservice
 *
 * Features:
 * ✅ Email notifications
 * ✅ SMS notifications
 * ✅ Event subscription (order.*, payment.*)
 * ✅ Rate limiting (20 emails/hour, 5 SMS/hour)
 * ✅ No database (stateless)
 *
 * Events listened:
 * - order.created
 * - order.status_changed
 * - payment.completed
 */

// ================================================================
// MAIN APPLICATION
// ================================================================
@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
		System.out.println("🚀 Notification Service started on port 8004");
	}
}
