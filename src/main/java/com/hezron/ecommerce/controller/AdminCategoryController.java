package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.CategoryDTO;
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
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Attempting to create new category: {}", categoryDTO.getName());
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Category created successfully", createdCategory));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Attempting to update category with ID: {}", id);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
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
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<CategoryDTO>>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Searching categories with filters: name={}, active={}", name, active);
        PagedResponseDTO<CategoryDTO> categories = categoryService.searchCategories(
                name, active, page, size, sortBy, sortDir);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Categories retrieved successfully", categories));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update category status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> updateCategoryStatus(
            @PathVariable Long id, 
            @RequestParam boolean active) {
        log.info("Updating category status for ID: {} to {}", id, active);
        CategoryDTO updatedCategory = categoryService.updateCategoryStatus(id, active);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Category status updated successfully", updatedCategory));
    }
}