package com.example.productservice.controller;

import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllProducts() throws Exception {
        Category category = new Category("Electronics");
        Product product1 = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        Product product2 = new Product("Phone", "Smartphone", BigDecimal.valueOf(500), category, 20, Arrays.asList("img2.jpg"));
        Page<Product> page = new PageImpl<>(Arrays.asList(product1, product2), PageRequest.of(0, 10), 2);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    public void testGetProductById_Found() throws Exception {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        product.setId(1L);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        product.setId(1L);
        when(productService.createProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateProduct_Invalid() throws Exception {
        Product invalidProduct = new Product("", "", null, null, null, null);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProduct_Found() throws Exception {
        Category category = new Category("Electronics");
        Product productDetails = new Product("Updated Laptop", "Updated Desc", BigDecimal.valueOf(1200), category, 15, Arrays.asList("img3.jpg"));
        Product updatedProduct = new Product("Updated Laptop", "Updated Desc", BigDecimal.valueOf(1200), category, 15, Arrays.asList("img3.jpg"));
        updatedProduct.setId(1L);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    public void testUpdateProduct_NotFound() throws Exception {
        Category category = new Category("Electronics");
        Product productDetails = new Product("Updated Laptop", "Updated Desc", BigDecimal.valueOf(1200), category, 15, Arrays.asList("img3.jpg"));
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(null);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    public void testSearchProducts() throws Exception {
        Category category = new Category("Electronics");
        Product product = new Product("Laptop", "Gaming Laptop", BigDecimal.valueOf(1000), category, 10, Arrays.asList("img1.jpg"));
        when(productService.searchProducts("laptop")).thenReturn(Arrays.asList(product));

        mockMvc.perform(get("/api/products/search?q=laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }
}