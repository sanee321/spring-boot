package com.example.orderservice;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Order Service using TestContainers
 * Tests service-to-service communication with real PostgreSQL database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderServiceIntegrationTest {

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
    private OrderRepository orderRepository;

    @Test
    void testOrderPersistenceWithRealDatabase() {
        // Arrange
        Order order = new Order();
        order.setUserId(123L);
        order.setTotalAmount(new BigDecimal("299.99"));
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Main St, City, State 12345");

        // Act
        Order savedOrder = orderRepository.save(order);

        // Assert
        assertNotNull(savedOrder.getId());
        Optional<Order> retrieved = orderRepository.findById(savedOrder.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(OrderStatus.PENDING, retrieved.get().getStatus());
        assertEquals("123 Main St, City, State 12345", retrieved.get().getShippingAddress());
    }

    @Test
    void testFindOrdersByUserId() {
        // Arrange
        Order order1 = new Order();
        order1.setUserId(456L);
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setStatus(OrderStatus.CONFIRMED);
        order1.setShippingAddress("456 Oak Ave");
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(456L);
        order2.setTotalAmount(new BigDecimal("150.00"));
        order2.setStatus(OrderStatus.SHIPPED);
        order2.setShippingAddress("456 Oak Ave");
        orderRepository.save(order2);

        // Act
        List<Order> orders = orderRepository.findByUserId(456L);

        // Assert
        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getUserId().equals(456L)));
    }

    @Test
    void testFindByIdAndUserId() {
        // Arrange
        Order order = new Order();
        order.setUserId(789L);
        order.setTotalAmount(new BigDecimal("249.99"));
        order.setStatus(OrderStatus.PROCESSING);
        order.setShippingAddress("789 Pine Ln");
        Order saved = orderRepository.save(order);

        // Act
        Optional<Order> found = orderRepository.findByIdAndUserId(saved.getId(), 789L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(OrderStatus.PROCESSING, found.get().getStatus());
    }

    @Test
    void testOrderStatusTransitions() {
        // Arrange
        Order order = new Order();
        order.setUserId(111L);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("111 Elm St");
        Order saved = orderRepository.save(order);

        // Act & Assert - Test status transitions
        saved.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(saved);
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(saved.getId()).get().getStatus());

        saved.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(saved);
        assertEquals(OrderStatus.PROCESSING, orderRepository.findById(saved.getId()).get().getStatus());

        saved.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(saved);
        assertEquals(OrderStatus.SHIPPED, orderRepository.findById(saved.getId()).get().getStatus());

        saved.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(saved);
        assertEquals(OrderStatus.DELIVERED, orderRepository.findById(saved.getId()).get().getStatus());
    }
}
