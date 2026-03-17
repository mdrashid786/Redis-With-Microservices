package com.order.service.controller;

import com.order.service.beans.Order;
import com.order.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        try {
            Order order = service.placeOrder(
                    request.userId,
                    request.productId,
                    request.quantity
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new OrderResponse("Order placed successfully", order)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Order order = service.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserOrders(userId));
    }

    static class OrderRequest {
        public Long userId;
        public Long productId;
        public Integer quantity;
    }

    static class OrderResponse {
        public String message;
        public Order order;

        public OrderResponse(String message, Order order) {
            this.message = message;
            this.order = order;
        }
    }

    static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
