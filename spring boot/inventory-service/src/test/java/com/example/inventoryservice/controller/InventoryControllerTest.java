package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAddInventory() throws Exception {
        InventoryRequest request = new InventoryRequest(1L, 100, "Warehouse A");
        InventoryResponse response = new InventoryResponse(1L, 1L, 100, 0, 100, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.addInventory(any(InventoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(inventoryService, times(1)).addInventory(any(InventoryRequest.class));
    }

    @Test
    void testGetInventoryById() throws Exception {
        InventoryResponse response = new InventoryResponse(1L, 1L, 100, 0, 100, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.getInventoryById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(inventoryService, times(1)).getInventoryById(1L);
    }

    @Test
    void testGetInventoryByIdNotFound() throws Exception {
        when(inventoryService.getInventoryById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/999"))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(999L);
    }

    @Test
    void testGetInventoryByProductId() throws Exception {
        InventoryResponse response = new InventoryResponse(1L, 1L, 100, 0, 100, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.getInventoryByProductId(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/inventory/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L));

        verify(inventoryService, times(1)).getInventoryByProductId(1L);
    }

    @Test
    void testUpdateQuantity() throws Exception {
        InventoryResponse response = new InventoryResponse(1L, 1L, 150, 0, 150, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.updateQuantity(1L, 150)).thenReturn(response);

        mockMvc.perform(put("/api/inventory/product/1/quantity")
                .param("quantity", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(150));

        verify(inventoryService, times(1)).updateQuantity(1L, 150);
    }

    @Test
    void testReserveStock() throws Exception {
        InventoryResponse response = new InventoryResponse(1L, 1L, 100, 30, 70, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.reserveStock(1L, 30)).thenReturn(response);

        mockMvc.perform(post("/api/inventory/product/1/reserve")
                .param("quantity", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserved").value(30))
                .andExpect(jsonPath("$.available").value(70));

        verify(inventoryService, times(1)).reserveStock(1L, 30);
    }

    @Test
    void testReleaseStock() throws Exception {
        InventoryResponse response = new InventoryResponse(1L, 1L, 100, 0, 100, "Warehouse A", LocalDateTime.now(), LocalDateTime.now());

        when(inventoryService.releaseStock(1L, 30)).thenReturn(response);

        mockMvc.perform(post("/api/inventory/product/1/release")
                .param("quantity", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(100));

        verify(inventoryService, times(1)).releaseStock(1L, 30);
    }

    @Test
    void testCheckAvailability() throws Exception {
        when(inventoryService.checkAvailability(1L, 50)).thenReturn(true);

        mockMvc.perform(get("/api/inventory/product/1/check")
                .param("quantity", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(inventoryService, times(1)).checkAvailability(1L, 50);
    }

    @Test
    void testGetAllInventory() throws Exception {
        List<InventoryResponse> responses = Arrays.asList(
                new InventoryResponse(1L, 1L, 100, 0, 100, "Warehouse A", LocalDateTime.now(), LocalDateTime.now()),
                new InventoryResponse(2L, 2L, 200, 50, 150, "Warehouse B", LocalDateTime.now(), LocalDateTime.now())
        );

        when(inventoryService.getAllInventory()).thenReturn(responses);

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L));

        verify(inventoryService, times(1)).getAllInventory();
    }

    @Test
    void testReserveStockNotFound() throws Exception {
        when(inventoryService.reserveStock(999L, 30)).thenReturn(null);

        mockMvc.perform(post("/api/inventory/product/999/reserve")
                .param("quantity", "30"))
                .andExpect(status().isBadRequest());

        verify(inventoryService, times(1)).reserveStock(999L, 30);
    }
}
