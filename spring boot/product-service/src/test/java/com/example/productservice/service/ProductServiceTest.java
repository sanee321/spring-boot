package com.example.productservice.service;

import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void testGetAllProducts() {
        Category category = new Category("Electronics");
        Product product1 = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product product2 = new Product("Phone", "Smartphone", BigDecimal.valueOf(500), category, 20, Arrays.asList("img2.jpg"));
        Page<Product> page = new PageImpl<>(Arrays.asList(product1, product2), PageRequest.of(0, 10), 2);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.getAllProducts(PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testGetProductById_Found() {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(1L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateProduct() {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertEquals(product, result);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void testUpdateProduct_Found() {
        Category category = new Category("Electronics");
        Product existingProduct = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        existingProduct.setId(1L);
        Product productDetails = new Product("Updated Laptop", "Updated Desc", BigDecimal.valueOf(1200), category, 15, Arrays.asList("img3.jpg"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, productDetails);

        assertNotNull(result);
        assertEquals("Updated Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(1200), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    public void testUpdateProduct_NotFound() {
        Category category = new Category("Electronics");
        Product productDetails = new Product("Updated Laptop", "Updated Desc", BigDecimal.valueOf(1200), category, 15, Arrays.asList("img3.jpg"));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Product result = productService.updateProduct(1L, productDetails);

        assertNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testSearchProducts() {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(Arrays.asList(product));

        List<Product> result = productService.searchProducts("laptop");

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("laptop");
    }
}