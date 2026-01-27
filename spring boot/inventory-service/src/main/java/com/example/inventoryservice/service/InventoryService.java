package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.entity.InventoryItem;
import com.example.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;

    public InventoryResponse addInventory(InventoryRequest request) {
        InventoryItem item = new InventoryItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setReserved(0);
        item.setWarehouse(request.getWarehouse());

        InventoryItem savedItem = inventoryRepository.save(item);
        return mapToResponse(savedItem);
    }

    public Optional<InventoryResponse> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId).map(this::mapToResponse);
    }

    public Optional<InventoryResponse> getInventoryById(Long id) {
        return inventoryRepository.findById(id).map(this::mapToResponse);
    }

    public InventoryResponse updateQuantity(Long productId, Integer quantity) {
        Optional<InventoryItem> optional = inventoryRepository.findByProductId(productId);
        if (optional.isPresent()) {
            InventoryItem item = optional.get();
            item.setQuantity(quantity);
            InventoryItem updatedItem = inventoryRepository.save(item);
            return mapToResponse(updatedItem);
        }
        return null;
    }

    public InventoryResponse reserveStock(Long productId, Integer quantity) {
        Optional<InventoryItem> optional = inventoryRepository.findByProductId(productId);
        if (optional.isPresent()) {
            InventoryItem item = optional.get();
            if (item.getAvailable() >= quantity) {
                item.setReserved(item.getReserved() + quantity);
                InventoryItem updatedItem = inventoryRepository.save(item);
                return mapToResponse(updatedItem);
            }
        }
        return null;
    }

    public InventoryResponse releaseStock(Long productId, Integer quantity) {
        Optional<InventoryItem> optional = inventoryRepository.findByProductId(productId);
        if (optional.isPresent()) {
            InventoryItem item = optional.get();
            int newReserved = Math.max(0, item.getReserved() - quantity);
            item.setReserved(newReserved);
            InventoryItem updatedItem = inventoryRepository.save(item);
            return mapToResponse(updatedItem);
        }
        return null;
    }

    public boolean checkAvailability(Long productId, Integer quantity) {
        Optional<InventoryItem> optional = inventoryRepository.findByProductId(productId);
        if (optional.isPresent()) {
            InventoryItem item = optional.get();
            return item.getAvailable() >= quantity;
        }
        return false;
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        InventoryResponse response = new InventoryResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setReserved(item.getReserved());
        response.setAvailable(item.getAvailable());
        response.setWarehouse(item.getWarehouse());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}
