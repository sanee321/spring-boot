package com.example.productservice.repository;

import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testSaveAndFindById() {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getId());

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertTrue(foundProduct.isPresent());
        assertEquals("Laptop", foundProduct.get().getName());
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        Category category = new Category("Electronics");
        Product product1 = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product product2 = new Product("Smartphone", "Android Phone", BigDecimal.valueOf(500), category, 20, Arrays.asList("img2.jpg"));
        productRepository.save(product1);
        productRepository.save(product2);

        List<Product> foundProducts = productRepository.findByNameContainingIgnoreCase("lap");
        assertEquals(1, foundProducts.size());
        assertEquals("Laptop", foundProducts.get(0).getName());
    }

    @Test
    public void testFindByCategoryId() {
        Category category1 = new Category("Electronics");
        Category category2 = new Category("Books");
        category1.setId(1L);
        category2.setId(2L);
        Product product1 = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category1, 10, Arrays.asList("img1.jpg"));
        Product product2 = new Product("Book", "Novel", BigDecimal.valueOf(20), category2, 5, Arrays.asList("img3.jpg"));
        productRepository.save(product1);
        productRepository.save(product2);

        List<Product> foundProducts = productRepository.findByCategoryId(1L);
        assertEquals(1, foundProducts.size());
        assertEquals("Laptop", foundProducts.get(0).getName());
    }

    @Test
    public void testFindAll() {
        Category category = new Category("Electronics");
        Product product1 = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product product2 = new Product("Phone", "Smartphone", BigDecimal.valueOf(500), category, 20, Arrays.asList("img2.jpg"));
        productRepository.save(product1);
        productRepository.save(product2);

        var products = productRepository.findAll();
        assertEquals(2, products.size());
    }

    @Test
    public void testDeleteById() {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product savedProduct = productRepository.save(product);

        productRepository.deleteById(savedProduct.getId());

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertFalse(foundProduct.isPresent());
    }
}