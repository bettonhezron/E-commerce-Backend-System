package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.ProductDTO;

import com.hezron.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling user-facing product operations
 * Provides endpoints for browsing and searching products
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Catalog", description = "APIs for browsing and searching products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all active products", description = "Retrieves a list of all active products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
            @ApiResponse(responseCode = "204", description = "No products found")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getAllProducts() {
        log.debug("Fetching all active products");
        List<ProductDTO> products = productService.getAllProducts();
        
        if (products.isEmpty()) {
            log.info("No active products found");
            return ResponseEntity.noContent().build();
        }
        
        log.info("Retrieved {} active products", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Successfully retrieved active products", 
                products
        ));
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProductById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        log.debug("Fetching product with ID: {}", id);
        ProductDTO product = productService.getProductById(id);
        
        log.info("Retrieved product: {}", product.getName());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Product retrieved successfully", 
                product
        ));
    }

    @Operation(summary = "Advanced Product Search", description = "Search products with multiple filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "204", description = "No products found matching criteria")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> advancedSearch(
            @Parameter(description = "Product name to search for")
            @RequestParam(required = false) String name,

            @Parameter(description = "Category ID to filter by")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Minimum price threshold")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "Maximum price threshold")
            @RequestParam(required = false) Double maxPrice,

            @Parameter(description = "Minimum stock threshold")
            @RequestParam(required = false) Integer minStock
    ) {
        log.debug("Performing advanced product search with parameters: name={}, categoryId={}, " +
                "price range=[{} to {}], minStock={}", 
                name, categoryId, minPrice, maxPrice, minStock);
        
        List<ProductDTO> products = productService.advancedSearch(name, categoryId, minPrice, maxPrice, minStock);
        
        if (products.isEmpty()) {
            log.info("No products found matching search criteria");
            return ResponseEntity.noContent().build();
        }
        
        log.info("Found {} products matching search criteria", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Search completed successfully", 
                products
        ));
    }

    @Operation(summary = "Get Newest Products", description = "Retrieves the 10 most recently added active products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved newest products")
    @GetMapping("/newest")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getNewestProducts() {
        log.debug("Fetching newest products");
        
        List<ProductDTO> products = productService.getNewestProducts();
        
        log.info("Retrieved {} newest products", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Newest products retrieved successfully", 
                products
        ));
    }

    @Operation(summary = "Get Premium Products", description = "Retrieves the 10 most expensive active products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved premium products")
    @GetMapping("/premium")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getPremiumProducts() {
        log.debug("Fetching premium products");
        
        List<ProductDTO> products = productService.getPremiumProducts();
        
        log.info("Retrieved {} premium products", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Premium products retrieved successfully", 
                products
        ));
    }

    @Operation(summary = "Get Budget Products", description = "Retrieves the 5 cheapest active products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved budget products")
    @GetMapping("/budget")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getBudgetProducts() {
        log.debug("Fetching budget products");
        
        List<ProductDTO> products = productService.getBudgetProducts();
        
        log.info("Retrieved {} budget products", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Budget products retrieved successfully", 
                products
        ));
    }

    @Operation(summary = "Get Low Stock Products", description = "Retrieves products with stock below a specified threshold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock products"),
            @ApiResponse(responseCode = "204", description = "No low stock products found")
    })
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getLowStockProducts(
            @Parameter(description = "Stock threshold - products with stock below this value will be returned")
            @RequestParam(defaultValue = "10") Integer threshold) {
        log.debug("Fetching low stock products with threshold: {}", threshold);
        
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        
        if (products.isEmpty()) {
            log.info("No products found with stock below threshold {}", threshold);
            return ResponseEntity.noContent().build();
        }
        
        log.info("Retrieved {} low stock products", products.size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Low stock products retrieved successfully", 
                products
        ));
    }
}