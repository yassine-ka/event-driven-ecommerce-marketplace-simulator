package com.example.inventory.web;

import com.example.inventory.application.InventoryService;
import com.example.inventory.domain.Product;
import com.example.inventory.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog API")
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all available products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = inventoryService.getAllProducts().stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its unique identifier")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") UUID id) {
        Product product = inventoryService.getProduct(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping("/inventory/{productId}")
    @Operation(summary = "Get inventory for product", description = "Retrieves current stock quantity for a product")
    public ResponseEntity<ProductResponse> getInventory(@PathVariable("productId") UUID productId) {
        Product product = inventoryService.getProduct(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
