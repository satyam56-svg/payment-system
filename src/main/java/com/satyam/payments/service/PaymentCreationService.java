package com.satyam.payments.service;

import com.satyam.payments.entity.Payment;
import com.satyam.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCreationService {

    private final PaymentRepository paymentRepository;

    public PaymentCreationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePayment(Payment payment) {
        return paymentRepository.saveAndFlush(payment);
    }
}