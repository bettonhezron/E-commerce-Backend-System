package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;
import com.hezron.ecommerce.dto.ProductDTO;

import com.hezron.ecommerce.service.ProductService;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling admin-level product operations
 * Provides endpoints for creating, updating, deleting, and searching products
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Product Management", description = "APIs for admin product management operations")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("Creating new product with name: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        log.info("Product created with ID: {}", createdProduct.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Product created successfully", createdProduct));
    }

    @Operation(summary = "Update an existing product", description = "Updates a product with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> updateProduct(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            log.info("Product updated successfully: {}", id);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product updated successfully", updatedProduct));
        } catch (ResourceNotFoundException ex) {
            log.error("Product not found with ID: {}", id);
            throw ex;
        }
    }

    @Operation(summary = "Delete a product", description = "Deletes a product with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @Parameter(description = "ID of the product to delete", required = true)
            @PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        try {
            productService.deleteProduct(id);
            log.info("Product deleted successfully: {}", id);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product deleted successfully", null));
        } catch (ResourceNotFoundException ex) {
            log.error("Product not found with ID: {}", id);
            throw ex;
        }
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a product with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProductById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        log.info("Fetching product with ID: {}", id);
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product retrieved successfully", product));
        } catch (ResourceNotFoundException ex) {
            log.error("Product not found with ID: {}", id);
            throw ex;
        }
    }

    @Operation(summary = "Search products with filters",
            description = "Searches for products based on various criteria with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<ProductDTO>>> adminSearch(
            @Parameter(description = "Product name to search for")
            @RequestParam(required = false) @Size(max = 255) String name,

            @Parameter(description = "Category ID to filter by")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Minimum price threshold")
            @RequestParam(required = false) @Min(0) Double minPrice,

            @Parameter(description = "Maximum price threshold")
            @RequestParam(required = false) @Min(0) Double maxPrice,

            @Parameter(description = "Active status filter")
            @RequestParam(required = false) Boolean active,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Performing admin search with filters: name={}, categoryId={}, price range=[{} to {}], active={}, page={}, size={}",
                name, categoryId, minPrice, maxPrice, active, page, size);

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        PagedResponseDTO<ProductDTO> products = productService.adminSearchPaginated(
                name, categoryId, minPrice, maxPrice, active, page, size, sortBy, sortDir);

        log.info("Admin search completed, found {} products", products.getTotalElements());
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products retrieved successfully", products));
    }

    @Operation(summary = "Get products with low stock",
            description = "Retrieves products that have stock levels below the specified threshold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock products"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getLowStockProducts(
            @Parameter(description = "Stock threshold - products with stock below this value will be returned")
            @RequestParam(defaultValue = "10") @Min(1) Integer threshold) {
        log.info("Fetching low stock products with threshold: {}", threshold);
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        log.info("Found {} products with stock below threshold {}", products.size(), threshold);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Low stock products retrieved successfully", products));
    }

    @Operation(summary = "Bulk update product status",
            description = "Updates the active status for multiple products at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/status")
    public ResponseEntity<ApiResponseDTO<Void>> bulkUpdateProductStatus(
            @Parameter(description = "List of product IDs to update")
            @RequestParam @Size(min = 1) List<Long> ids,

            @Parameter(description = "New active status")
            @RequestParam boolean active) {
        log.info("Bulk updating product status to {} for {} products", active, ids.size());
        productService.bulkUpdateProductStatus(ids, active);
        log.info("Bulk status update completed successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                String.format("Updated %d products to %s status", ids.size(), active ? "active" : "inactive"), null));
    }

    @Operation(summary = "Bulk update product stock",
            description = "Updates stock quantities for multiple products at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/stock")
    public ResponseEntity<ApiResponseDTO<Void>> bulkUpdateStock(
            @Valid @RequestBody @Size(min = 1) List<ProductDTO> products) {
        log.info("Performing bulk stock update for {} products", products.size());
        productService.bulkUpdateStock(products);
        log.info("Bulk stock update completed successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                String.format("Updated stock for %d products", products.size()), null));
    }
}