package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testProcessPayment() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD");
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "COMPLETED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).processPayment(any(PaymentRequest.class));
    }

    @Test
    public void testGetPaymentById() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "COMPLETED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).getPaymentById(1L);
    }

    @Test
    public void testGetPaymentById_NotFound() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).getPaymentById(1L);
    }

    @Test
    public void testGetPaymentByOrderId() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "COMPLETED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));

        verify(paymentService, times(1)).getPaymentByOrderId(1L);
    }

    @Test
    public void testGetPaymentsByUserId() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "COMPLETED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.getPaymentsByUserId(1L)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).getPaymentsByUserId(1L);
    }

    @Test
    public void testRefundPayment() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "REFUNDED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.refundPayment(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        verify(paymentService, times(1)).refundPayment(1L);
    }

    @Test
    public void testGetAllPayments() throws Exception {
        PaymentResponse response = new PaymentResponse(1L, 1L, 1L, new BigDecimal("100.00"), "USD", "CREDIT_CARD", 
                "COMPLETED", "txn-123", LocalDateTime.now(), LocalDateTime.now());

        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).getAllPayments();
    }
}
