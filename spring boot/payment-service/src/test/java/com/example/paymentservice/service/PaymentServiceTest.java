package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void testProcessPayment() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD");
        
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn-123");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    public void testGetPaymentById_Found() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn-123");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<PaymentResponse> response = paymentService.getPaymentById(1L);

        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getId());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetPaymentById_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<PaymentResponse> response = paymentService.getPaymentById(1L);

        assertFalse(response.isPresent());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetPaymentByOrderId() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        Optional<PaymentResponse> response = paymentService.getPaymentByOrderId(1L);

        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getOrderId());
        verify(paymentRepository, times(1)).findByOrderId(1L);
    }

    @Test
    public void testGetPaymentsByUserId() {
        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setUserId(1L);
        payment1.setOrderId(1L);

        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setUserId(1L);
        payment2.setOrderId(2L);

        when(paymentRepository.findByUserId(1L)).thenReturn(Arrays.asList(payment1, payment2));

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(1L);

        assertEquals(2, responses.size());
        verify(paymentRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testRefundPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.refundPayment(1L);

        assertNotNull(response);
        assertEquals("REFUNDED", response.getStatus());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    public void testRefundPayment_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        PaymentResponse response = paymentService.refundPayment(1L);

        assertNull(response);
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllPayments() {
        Payment payment1 = new Payment();
        payment1.setId(1L);
        Payment payment2 = new Payment();
        payment2.setId(2L);

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        List<PaymentResponse> responses = paymentService.getAllPayments();

        assertEquals(2, responses.size());
        verify(paymentRepository, times(1)).findAll();
    }
}
