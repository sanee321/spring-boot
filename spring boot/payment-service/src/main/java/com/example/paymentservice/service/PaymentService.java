package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionId(UUID.randomUUID().toString());

        // Simulate payment processing
        payment.setStatus(PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    public Optional<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id).map(this::mapToResponse);
    }

    public Optional<PaymentResponse> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).map(this::mapToResponse);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse refundPayment(Long paymentId) {
        Optional<Payment> optional = paymentRepository.findById(paymentId);
        if (optional.isPresent()) {
            Payment payment = optional.get();
            payment.setStatus(PaymentStatus.REFUNDED);
            Payment refundedPayment = paymentRepository.save(payment);
            return mapToResponse(refundedPayment);
        }
        return null;
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus().toString());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
