package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.addInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/product/{productId}/quantity")
    public ResponseEntity<InventoryResponse> updateQuantity(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.updateQuantity(productId, quantity);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/product/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.reserveStock(productId, quantity);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/product/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.releaseStock(productId, quantity);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/product/{productId}/check")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long productId, @RequestParam Integer quantity) {
        boolean available = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        List<InventoryResponse> items = inventoryService.getAllInventory();
        return ResponseEntity.ok(items);
    }
}
