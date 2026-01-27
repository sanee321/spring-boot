package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateOrder() throws Exception {
        OrderItem item = new OrderItem(1L, 2, BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        order.setId(1L);
        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateOrder_Invalid() throws Exception {
        Order invalidOrder = new Order(null, null, null, null);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetOrderById_Found() throws Exception {
        OrderItem item = new OrderItem(1L, 2, BigDecimal.valueOf(100));
        Order order = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        order.setId(1L);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetOrdersByUserId() throws Exception {
        OrderItem item = new OrderItem(1L, 2, BigDecimal.valueOf(100));
        Order order1 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "PENDING");
        Order order2 = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(300), "COMPLETED");
        when(orderService.getOrdersByUserId(1L)).thenReturn(Arrays.asList(order1, order2));

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testUpdateOrderStatus_Found() throws Exception {
        OrderItem item = new OrderItem(1L, 2, BigDecimal.valueOf(100));
        Order updatedOrder = new Order(1L, Arrays.asList(item), BigDecimal.valueOf(200), "COMPLETED");
        updatedOrder.setId(1L);
        when(orderService.updateOrderStatus(eq(1L), eq("COMPLETED"))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/orders/1/status?status=COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testUpdateOrderStatus_NotFound() throws Exception {
        when(orderService.updateOrderStatus(eq(1L), eq("COMPLETED"))).thenReturn(null);

        mockMvc.perform(put("/api/orders/1/status?status=COMPLETED"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }
}