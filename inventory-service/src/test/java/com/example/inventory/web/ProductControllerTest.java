package com.example.inventory.web;

import com.example.inventory.application.InventoryService;
import com.example.inventory.domain.Product;
import com.example.inventory.web.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        Product product1 = Product.builder()
                .id(productId1)
                .sku("TEST-001")
                .name("Product 1")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(10)
                .build();

        Product product2 = Product.builder()
                .id(productId2)
                .sku("TEST-002")
                .name("Product 2")
                .price(BigDecimal.valueOf(75.00))
                .stockQuantity(20)
                .build();

        when(inventoryService.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(productId1.toString()))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].id").value(productId2.toString()));
    }

    @Test
    void shouldGetProductById() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(10)
                .build();

        when(inventoryService.getProduct(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void shouldGetInventoryForProduct() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .sku("TEST-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(15)
                .build();

        when(inventoryService.getProduct(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/products/inventory/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.stockQuantity").value(15));
    }
}
