package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ProductDTO;
import com.hezron.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
public class PublicProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/search/advanced")
    public ResponseEntity<List<ProductDTO>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock) {
        List<ProductDTO> products = productService.advancedSearch(
                name, categoryId, minPrice, maxPrice, minStock);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductDTO>> getNewArrivals() {
        List<ProductDTO> products = productService.getNewestProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/premium")
    public ResponseEntity<List<ProductDTO>> getPremiumProducts() {
        List<ProductDTO> products = productService.getPremiumProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/budget")
    public ResponseEntity<List<ProductDTO>> getBudgetProducts() {
        List<ProductDTO> products = productService.getBudgetProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        // Implement this method in your service
        // For now returning empty list
        return ResponseEntity.ok(List.of());
    }
}