package com.payment.service.service;

import com.payment.service.beans.IdempotencyRecord;
import com.payment.service.beans.Payment;
import com.payment.service.repository.IdempotencyRepository;
import com.payment.service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 🔥 PROCESS PAYMENT - With idempotency
     */
    public Payment processPayment(Long orderId, Long userId, BigDecimal amount,
                                  String idempotencyKey) throws InterruptedException {

        // STEP 1: Check idempotency (prevent double charging)
        Optional<IdempotencyRecord> existing =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            System.out.println("✅ Idempotent request (duplicate): " + idempotencyKey);

            Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
            if (payment.isPresent()) {
                return payment.get(); // Return cached result
            }
        }

        // STEP 2: Rate limit check (10 payments/hour)
        Long requestCount = redisTemplate.opsForValue()
                .increment("rate_limit:payment:" + userId);

        if (requestCount == 1) {
            redisTemplate.expire("rate_limit:payment:" + userId, 3600, TimeUnit.SECONDS);
        }

        if (requestCount > 4) {
            throw new RuntimeException("Rate limit exceeded. Max 10 payments per hour");
        }

        // STEP 3: Acquire lock (prevent concurrent payments for same order)
        String lockKey = "payment_lock:order:" + orderId;
        String lockToken = UUID.randomUUID().toString();

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, 60, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(locked)) {
            throw new RuntimeException("Payment already in progress for this order");
        }

        try {
            System.out.println("🔒 Lock acquired for order: " + orderId);

            // STEP 4: Record idempotency (before processing)
            IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, orderId);
            idempotencyRepository.save(record);

            // STEP 5: Process payment (call payment gateway)
            System.out.println("💳 Processing payment...");
            // Simulate payment processing
            Thread.sleep(500);

            // STEP 6: Create payment record
            Payment payment = new Payment(orderId, userId, amount);
            payment.setIdempotencyKey(idempotencyKey);
            payment.setStatus(Payment.PaymentStatus.SUCCESS);

            Payment savedPayment = paymentRepository.save(payment);

            System.out.println("✅ Payment processed: " + savedPayment.getTransactionId());

            // STEP 7: Publish event
            publishEvent("payment.completed",
                    "PAYMENT_COMPLETED:{orderId:" + orderId + ",amount:" + amount + "}");

            return savedPayment;

        } finally {
            // STEP 8: Release lock
            String current = (String) redisTemplate.opsForValue().get(lockKey);
            if (lockToken.equals(current)) {
                redisTemplate.delete(lockKey);
            }
            System.out.println("🔓 Lock released");
        }
    }

    private void publishEvent(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
        System.out.println("📢 Event published: " + channel);
    }
}