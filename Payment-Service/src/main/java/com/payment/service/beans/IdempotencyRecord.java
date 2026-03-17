package com.payment.service.beans;


import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord implements Serializable {

    private static final long serialVersionUID = 1L; // version control

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long orderId;

    private String result;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IdempotencyRecord() {}
    public IdempotencyRecord(String key, Long orderId) {
        this.idempotencyKey = key;
        this.orderId = orderId;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getOrderId() { return orderId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

