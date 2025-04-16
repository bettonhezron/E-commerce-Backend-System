package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.CategoryResponseDTO;
import com.hezron.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Catalog", description = "APIs for browsing product categories")
public class UserCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getActiveCategories() {
        List<CategoryResponseDTO> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Active categories retrieved successfully",
                categories
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Category retrieved successfully",
                category
        ));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category hierarchy")
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getCategoryTree() {
        List<CategoryResponseDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(new ApiResponseDTO<>(
                true,
                "Category hierarchy retrieved successfully",
                categoryTree
        ));
    }
}