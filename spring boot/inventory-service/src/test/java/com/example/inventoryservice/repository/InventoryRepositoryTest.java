package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.InventoryItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void testSaveInventoryItem() {
        InventoryItem item = new InventoryItem();
        item.setProductId(1L);
        item.setQuantity(100);
        item.setReserved(0);
        item.setWarehouse("Warehouse A");

        InventoryItem saved = inventoryRepository.save(item);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getProductId());
        assertEquals(100, saved.getQuantity());
    }

    @Test
    void testFindByProductId() {
        InventoryItem item = new InventoryItem();
        item.setProductId(2L);
        item.setQuantity(50);
        item.setReserved(10);
        item.setWarehouse("Warehouse B");

        inventoryRepository.save(item);

        Optional<InventoryItem> found = inventoryRepository.findByProductId(2L);

        assertTrue(found.isPresent());
        assertEquals(2L, found.get().getProductId());
        assertEquals(50, found.get().getQuantity());
        assertEquals(10, found.get().getReserved());
    }

    @Test
    void testFindByProductIdNotFound() {
        Optional<InventoryItem> found = inventoryRepository.findByProductId(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateInventoryItem() {
        InventoryItem item = new InventoryItem();
        item.setProductId(3L);
        item.setQuantity(75);
        item.setReserved(5);
        item.setWarehouse("Warehouse C");

        InventoryItem saved = inventoryRepository.save(item);
        Long itemId = saved.getId();

        saved.setQuantity(100);
        saved.setReserved(25);
        inventoryRepository.save(saved);

        Optional<InventoryItem> updated = inventoryRepository.findById(itemId);

        assertTrue(updated.isPresent());
        assertEquals(100, updated.get().getQuantity());
        assertEquals(25, updated.get().getReserved());
    }

    @Test
    void testDeleteInventoryItem() {
        InventoryItem item = new InventoryItem();
        item.setProductId(4L);
        item.setQuantity(40);
        item.setReserved(0);
        item.setWarehouse("Warehouse D");

        InventoryItem saved = inventoryRepository.save(item);
        Long itemId = saved.getId();

        inventoryRepository.deleteById(itemId);

        Optional<InventoryItem> deleted = inventoryRepository.findById(itemId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAll() {
        InventoryItem item1 = new InventoryItem();
        item1.setProductId(5L);
        item1.setQuantity(100);
        item1.setReserved(0);

        InventoryItem item2 = new InventoryItem();
        item2.setProductId(6L);
        item2.setQuantity(200);
        item2.setReserved(50);

        inventoryRepository.save(item1);
        inventoryRepository.save(item2);

        var all = inventoryRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testInventoryItemWithWarehouse() {
        InventoryItem item = new InventoryItem();
        item.setProductId(7L);
        item.setQuantity(150);
        item.setReserved(30);
        item.setWarehouse("Central Warehouse");

        InventoryItem saved = inventoryRepository.save(item);

        assertEquals("Central Warehouse", saved.getWarehouse());
    }

    @Test
    void testGetAvailableQuantity() {
        InventoryItem item = new InventoryItem();
        item.setProductId(8L);
        item.setQuantity(100);
        item.setReserved(30);

        InventoryItem saved = inventoryRepository.save(item);

        assertEquals(70, saved.getAvailable());
    }
}
