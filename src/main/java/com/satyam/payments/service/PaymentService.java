package com.satyam.payments.service;

import com.satyam.payments.entity.Payment;
import com.satyam.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.satyam.payments.exception.PaymentNotFoundException;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCreationService paymentCreationService;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentCreationService paymentCreationService) {
        this.paymentRepository = paymentRepository;
        this.paymentCreationService = paymentCreationService;
    }

    @Transactional
    public Payment createPayment(
            BigDecimal amount,
            String currency,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required");
        }

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must not exceed 100 characters");
        }

        var existingPayment = paymentRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();

            if (existing.getAmount().compareTo(amount) != 0
                    || !existing.getCurrency().equals(currency)) {
                throw new IllegalArgumentException(
                        "Idempotency-Key already used with different payment details");
            }

            return existing;
        }

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setCurrency("INR");
        payment.setIdempotencyKey(idempotencyKey);

        try {
            return paymentCreationService.savePayment(payment);
        } catch (DataIntegrityViolationException ex) {
            Payment existing = paymentRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);

            if (existing.getAmount().compareTo(amount) != 0
                    || !existing.getCurrency().equals(currency)) {
                throw new IllegalArgumentException(
                        "Idempotency-Key already used with different payment details");
            }

            return existing;
        }
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }

        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + id));
    }
}
