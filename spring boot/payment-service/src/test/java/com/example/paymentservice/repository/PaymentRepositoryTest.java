package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void testSavePayment() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn-123");

        Payment savedPayment = paymentRepository.save(payment);

        assertNotNull(savedPayment.getId());
        assertEquals(1L, savedPayment.getOrderId());
    }

    @Test
    public void testFindByOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);

        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByOrderId(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getOrderId());
    }

    @Test
    public void testFindByUserId() {
        Payment payment1 = new Payment();
        payment1.setOrderId(1L);
        payment1.setUserId(1L);
        payment1.setAmount(new BigDecimal("100.00"));
        payment1.setCurrency("USD");
        payment1.setMethod("CREDIT_CARD");
        payment1.setStatus(PaymentStatus.COMPLETED);

        Payment payment2 = new Payment();
        payment2.setOrderId(2L);
        payment2.setUserId(1L);
        payment2.setAmount(new BigDecimal("200.00"));
        payment2.setCurrency("USD");
        payment2.setMethod("CREDIT_CARD");
        payment2.setStatus(PaymentStatus.COMPLETED);

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        List<Payment> payments = paymentRepository.findByUserId(1L);

        assertEquals(2, payments.size());
    }

    @Test
    public void testFindByTransactionId() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn-123");

        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByTransactionId("txn-123");

        assertTrue(found.isPresent());
        assertEquals("txn-123", found.get().getTransactionId());
    }

    @Test
    public void testDeletePayment() {
        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);
        paymentRepository.deleteById(savedPayment.getId());

        Optional<Payment> found = paymentRepository.findById(savedPayment.getId());

        assertFalse(found.isPresent());
    }
}
