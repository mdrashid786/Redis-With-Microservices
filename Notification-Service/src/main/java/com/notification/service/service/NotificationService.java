package com.notification.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 🔥 SEND EMAIL
     */
    public void sendEmail(String to, String subject, String body) throws Exception {
        // Rate limit: 20 emails/hour
        Long emailCount = redisTemplate.opsForValue()
                .increment("rate_limit:email:" + to);

        if (emailCount == 1) {
            redisTemplate.expire("rate_limit:email:" + to, 3600, TimeUnit.SECONDS);
        }

        if (emailCount > 20) {
            throw new RuntimeException("Email rate limit exceeded");
        }

        System.out.println("📧 Sending email to: " + to);
        System.out.println("   Subject: " + subject);
        System.out.println("   Body: " + body);

        // Simulate email sending
        // In real scenario: call SendGrid/AWS SES API
        Thread.sleep(100);

        System.out.println("✅ Email sent");
    }

    /**
     * 🔥 SEND SMS
     */
    public void sendSMS(String phone, String message) throws Exception {
        // Rate limit: 5 SMS/hour
        Long smsCount = redisTemplate.opsForValue()
                .increment("rate_limit:sms:" + phone);

        if (smsCount == 1) {
            redisTemplate.expire("rate_limit:sms:" + phone, 3600, TimeUnit.SECONDS);
        }

        if (smsCount > 5) {
            throw new RuntimeException("SMS rate limit exceeded");
        }

        System.out.println("📱 Sending SMS to: " + phone);
        System.out.println("   Message: " + message);

        // Simulate SMS sending
        // In real scenario: call Twilio/AWS SNS API
        Thread.sleep(100);

        System.out.println("✅ SMS sent");
    }
}
