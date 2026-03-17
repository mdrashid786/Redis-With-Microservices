package com.order.service.service;

import com.order.service.beans.Order;
import com.order.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Autowired
//    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 🔥 PLACE ORDER - Main operation with locking
     *
     * Flow:
     * 1. Rate limit check
     * 2. Acquire lock
     * 3. Get product stock from Product Service
     * 4. Check inventory
     * 5. Deduct stock
     * 6. Create order
     * 7. Publish event
     * 8. Release lock
     */
    public Order placeOrder(Long userId, Long productId, Integer quantity)
            throws InterruptedException {

        // STEP 1: Rate limit check (100 orders/day)
        Long requestCount = redisTemplate.opsForValue()
                .increment("rate_limit:order:" + userId);

        if (requestCount == 1) {
            redisTemplate.expire("rate_limit:order:" + userId, 1, TimeUnit.DAYS);
        }

        if (requestCount > 5) {
            throw new RuntimeException("Rate limit exceeded. Max 100 orders per day");
        }

        System.out.println("✅ Rate limit OK for user: " + userId);

        // STEP 2: Acquire lock
        String lockKey = "order_lock:product:" + productId;
        String lockToken = acquireLock(lockKey);

        if (lockToken == null) {
            throw new RuntimeException("Could not acquire lock");
        }

        try {
            System.out.println("🔒 Lock acquired for product: " + productId);

            // STEP 3: Get stock from Product Service
            Integer currentStock = getProductStock(productId);
            if (currentStock == null) {
                throw new RuntimeException("Product not found");
            }

            // STEP 4: Check inventory
            if (currentStock < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + currentStock);
            }

            System.out.println("✅ Inventory check OK");

            // STEP 5: Deduct stock
            Integer newStock = currentStock - quantity;
            updateProductStock(productId, newStock);

            // STEP 6: Create order
            BigDecimal totalPrice = BigDecimal.valueOf(100)
                    .multiply(BigDecimal.valueOf(quantity));

            Order order = new Order(userId, productId, quantity, totalPrice);
            Order savedOrder = repository.save(order);

            System.out.println("✅ Order created: " + savedOrder.getOrderNumber());

            // STEP 7: Publish event
            publishEvent("order.created",
                    "ORDER_CREATED:{orderId:" + savedOrder.getId() +
                            ",userId:" + userId + ",productId:" + productId + "}");

            // Cache order
            redisTemplate.opsForValue().set(
                    "order:" + savedOrder.getId(),
                    savedOrder,
                    1,
                    TimeUnit.HOURS
            );

            return savedOrder;

        } finally {
            // STEP 8: Release lock
            releaseLock(lockKey, lockToken);
            System.out.println("🔓 Lock released");
        }
    }

    @Cacheable(value = "order_by_id", key = "#id")
    public Order getOrderById(Long id) {
        System.out.println("🔍 DB Hit: getOrderById(" + id + ")");
        return repository.findById(id).orElse(null);
    }

    @Cacheable(value = "user_orders", key = "#userId")
    public List<Order> getUserOrders(Long userId) {
        System.out.println("🔍 DB Hit: getUserOrders(" + userId + ")");
        return repository.findByUserId(userId);
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = repository.findById(id).orElseThrow();
        order.setStatus(status);
        Order updated = repository.save(order);

        // Clear cache
        redisTemplate.delete("order:" + id);

        // Publish event
        publishEvent("order.status_changed",
                "ORDER_STATUS_CHANGED:{orderId:" + id + ",status:" + status + "}");

        return updated;
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private String acquireLock(String lockKey) throws InterruptedException {
        String lockToken = UUID.randomUUID().toString();
        int attempts = 0;

        while (attempts < 10) {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockToken, 30, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(acquired)) {
                return lockToken;
            }

            Thread.sleep((long) Math.pow(2, attempts) * 10);
            attempts++;
        }

        return null;
    }

    private void releaseLock(String lockKey, String lockToken) {
        String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
        if (lockToken != null && lockToken.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }


    private Integer getProductStock(Long productId) {
        try {

            String url = "http://localhost:8001/api/products/" + productId + "/stock";

            Integer stock = restTemplate.getForObject(url, Integer.class);

            return stock;

        } catch (Exception e) {
            System.err.println("❌ Product Service call failed: " + e.getMessage());
            return null;
        }
    }


    private void updateProductStock(Long productId, Integer newStock) {
        try {

            String url = "http://localhost:8001/api/products/{productId}/stock/{stock}";

            restTemplate.postForEntity(url, null, String.class, productId, newStock);

            System.out.println("✅ Stock updated via Product Service");

        } catch (Exception e) {
            System.err.println("❌ Stock update failed: " + e.getMessage());
        }
    }


    private void publishEvent(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
        System.out.println("📢 Event published: " + channel + " → " + message);
    }
}

