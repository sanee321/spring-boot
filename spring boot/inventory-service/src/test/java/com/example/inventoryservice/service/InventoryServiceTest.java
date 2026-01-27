package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.entity.InventoryItem;
import com.example.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    public void testAddInventory() {
        InventoryRequest request = new InventoryRequest(1L, 100, "Warehouse A");
        
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(100);
        item.setReserved(0);
        item.setWarehouse("Warehouse A");
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        InventoryResponse response = inventoryService.addInventory(request);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(100, response.getQuantity());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    public void testGetInventoryByProductId() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(100);
        item.setReserved(0);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));

        Optional<InventoryResponse> response = inventoryService.getInventoryByProductId(1L);

        assertTrue(response.isPresent());
        assertEquals(100, response.get().getAvailable());
        verify(inventoryRepository, times(1)).findByProductId(1L);
    }

    @Test
    public void testReserveStock() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(100);
        item.setReserved(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        InventoryResponse response = inventoryService.reserveStock(1L, 30);

        assertNotNull(response);
        assertEquals(70, response.getAvailable());
        verify(inventoryRepository, times(1)).findByProductId(1L);
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    public void testCheckAvailability_True() {
        InventoryItem item = new InventoryItem();
        item.setQuantity(100);
        item.setReserved(0);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));

        boolean available = inventoryService.checkAvailability(1L, 50);

        assertTrue(available);
        verify(inventoryRepository, times(1)).findByProductId(1L);
    }

    @Test
    public void testCheckAvailability_False() {
        InventoryItem item = new InventoryItem();
        item.setQuantity(100);
        item.setReserved(80);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));

        boolean available = inventoryService.checkAvailability(1L, 50);

        assertFalse(available);
        verify(inventoryRepository, times(1)).findByProductId(1L);
    }
}
