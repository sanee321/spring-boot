package com.example.paymentservice;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Payment Service using TestContainers
 * Tests service-to-service communication with a real PostgreSQL database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void testPaymentPersistenceWithRealDatabase() {
        // Arrange
        Payment payment = new Payment();
        payment.setOrderId(123L);
        payment.setUserId(456L);
        payment.setAmount(new BigDecimal("99.99"));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.PENDING);

        // Act
        Payment savedPayment = paymentRepository.save(payment);

        // Assert
        assertNotNull(savedPayment.getId());
        Optional<Payment> retrievedPayment = paymentRepository.findById(savedPayment.getId());
        assertTrue(retrievedPayment.isPresent());
        assertEquals("CREDIT_CARD", retrievedPayment.get().getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, retrievedPayment.get().getStatus());
    }

    @Test
    void testFindPaymentsByUserId() {
        // Arrange
        Payment payment1 = new Payment();
        payment1.setOrderId(111L);
        payment1.setUserId(789L);
        payment1.setAmount(new BigDecimal("50.00"));
        payment1.setPaymentMethod("DEBIT_CARD");
        payment1.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment1);

        Payment payment2 = new Payment();
        payment2.setOrderId(112L);
        payment2.setUserId(789L);
        payment2.setAmount(new BigDecimal("75.00"));
        payment2.setPaymentMethod("CREDIT_CARD");
        payment2.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment2);

        // Act
        var payments = paymentRepository.findByUserId(789L);

        // Assert
        assertEquals(2, payments.size());
    }

    @Test
    void testFindPaymentsByOrderId() {
        // Arrange
        Payment payment = new Payment();
        payment.setOrderId(999L);
        payment.setUserId(111L);
        payment.setAmount(new BigDecimal("199.99"));
        payment.setPaymentMethod("PAYPAL");
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        // Act
        Optional<Payment> found = paymentRepository.findByOrderId(999L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(PaymentStatus.PROCESSING, found.get().getStatus());
        assertEquals("PAYPAL", found.get().getPaymentMethod());
    }
}
