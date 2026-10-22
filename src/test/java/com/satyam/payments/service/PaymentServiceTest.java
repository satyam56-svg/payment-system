package com.satyam.payments.service;

import com.satyam.payments.entity.Payment;
import com.satyam.payments.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCreationService paymentCreationService;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setAmount(new BigDecimal("199.00"));
        payment.setCurrency("INR");
        payment.setIdempotencyKey("test-key-001");
    }

    @Test
    void shouldCreateNewPayment() {
        when(paymentRepository.findByIdempotencyKey("test-key-001"))
                .thenReturn(Optional.empty());

        when(paymentCreationService.savePayment(any(Payment.class)))
                .thenReturn(payment);

        Payment result = paymentService.createPayment(
                new BigDecimal("199.00"), "INR", "test-key-001");

        assertNotNull(result);
        assertEquals(new BigDecimal("199.00"), result.getAmount());
        assertEquals("INR", result.getCurrency());

        verify(paymentCreationService, times(1))
                .savePayment(any(Payment.class));
    }

    @Test
    void shouldReturnExistingPaymentForSameIdempotencyKey() {
        when(paymentRepository.findByIdempotencyKey("test-key-001"))
                .thenReturn(Optional.of(payment));

        Payment result = paymentService.createPayment(
                new BigDecimal("199.00"), "INR", "test-key-001");

        assertSame(payment, result);

        verify(paymentCreationService, never())
                .savePayment(any(Payment.class));
    }

    @Test
    void shouldRejectDifferentAmountForSameIdempotencyKey() {
        when(paymentRepository.findByIdempotencyKey("test-key-001"))
                .thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPayment(
                        new BigDecimal("299.00"), "INR", "test-key-001"));

        verify(paymentCreationService, never())
                .savePayment(any(Payment.class));
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPayment(
                        new BigDecimal("199.00"), "INR", null));

        verifyNoInteractions(paymentRepository, paymentCreationService);
    }
}