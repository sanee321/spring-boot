package com.example.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer reserved;
    private Integer available;
    private String warehouse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
