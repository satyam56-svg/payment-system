package com.satyam.payments.dto;

import com.satyam.payments.entity.Payment;
import com.satyam.payments.entity.PaymentStatus;

import java.math.BigDecimal;

public class PaymentResponse {

    private final Long id;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentStatus status;
    private final String idempotencyKey;

    public PaymentResponse(
            Long id,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String idempotencyKey) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getIdempotencyKey()
        );
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}