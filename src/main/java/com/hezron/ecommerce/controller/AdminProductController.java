package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ProductDTO;
import com.hezron.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> adminSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean active
    ) {
        List<ProductDTO> products = productService.adminSearch(name, categoryId, minPrice, maxPrice, active);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductDTO> products = productService.getLowStocksProducts(threshold);
        return ResponseEntity.ok(products);
    }
}