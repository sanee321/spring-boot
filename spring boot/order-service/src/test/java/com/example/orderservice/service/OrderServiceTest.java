package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testGetAllOrders() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order1 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order order2 = new Order(2L, Arrays.asList(item), BigDecimal.valueOf(300), "COMPLETED");
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        List<Order> orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testGetOrderById_Found() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals("PENDING", result.get().getStatus());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(1L);

        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateOrder() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertEquals(order, result);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testUpdateOrderStatus_Found() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order existingOrder = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        existingOrder.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        Order result = orderService.updateOrderStatus(1L, "COMPLETED");

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(existingOrder);
    }

    @Test
    public void testUpdateOrderStatus_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Order result = orderService.updateOrderStatus(1L, "COMPLETED");

        assertNull(result);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testDeleteOrder() {
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetOrdersByUserId() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        Order order1 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order order2 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(300), "COMPLETED");
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(order1, order2));

        List<Order> result = orderService.getOrdersByUserId(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getUserId());
        verify(orderRepository, times(1)).findByUserId(1L);
    }
}