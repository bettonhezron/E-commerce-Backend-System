package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.CategoryRequestDTO;
import com.hezron.ecommerce.dto.CategoryResponseDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;
import com.hezron.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Category Management", description = "APIs for managing product categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO categoryDTO) {
        log.info("Attempting to create new category: {}", categoryDTO.getName());
        CategoryResponseDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Category created successfully", createdCategory));
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getAllCategories() {
        log.info("Retrieving all categories");
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Categories retrieved successfully", categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        log.info("Retrieving category with ID: {}", id);
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category retrieved successfully", category));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getActiveCategories() {
        log.info("Retrieving all active categories");
        List<CategoryResponseDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Active categories retrieved successfully", categories));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree structure")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category tree retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getCategoryTree() {
        log.info("Retrieving category tree structure");
        List<CategoryResponseDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category tree retrieved successfully", categoryTree));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO categoryDTO) {
        log.info("Attempting to update category with ID: {}", id);
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(@PathVariable Long id) {
        log.info("Attempting to delete category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<CategoryResponseDTO>>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Searching categories with filters: name={}, active={}", name, active);
        PagedResponseDTO<CategoryResponseDTO> categories = categoryService.searchCategories(
                name, active, page, size, sortBy, sortDir);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Categories retrieved successfully", categories));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update category status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("Updating category status for ID: {} to {}", id, active);
        CategoryResponseDTO updatedCategory = categoryService.updateCategoryStatus(id, active);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category status updated successfully", updatedCategory));
    }
}