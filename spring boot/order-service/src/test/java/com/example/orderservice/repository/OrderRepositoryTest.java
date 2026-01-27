package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testSaveAndFindById() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder.getId());

        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(foundOrder.isPresent());
        assertEquals("PENDING", foundOrder.get().getStatus());
    }

    @Test
    public void testFindByUserId() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order1 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order order2 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(300), "COMPLETED");
        Order order3 = new Order(2L, Arrays.asList(item), BigDecimal.valueOf(400), "PENDING");
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> foundOrders = orderRepository.findByUserId(1L);
        assertEquals(2, foundOrders.size());
        assertEquals(1L, foundOrders.get(0).getUserId());
    }

    @Test
    public void testFindAll() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order1 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order order2 = new Order(2L, Arrays.asList(item), BigDecimal.valueOf(300), "COMPLETED");
        orderRepository.save(order1);
        orderRepository.save(order2);

        var orders = orderRepository.findAll();
        assertEquals(2, orders.size());
    }

    @Test
    public void testDeleteById() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order savedOrder = orderRepository.save(order);

        orderRepository.deleteById(savedOrder.getId());

        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());
        assertFalse(foundOrder.isPresent());
    }
}