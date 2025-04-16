package com.hezron.ecommerce.service;


import com.hezron.ecommerce.dto.CategoryRequestDTO;
import com.hezron.ecommerce.dto.CategoryResponseDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;

import java.util.List;

public interface CategoryService {
    // Create a new category
    CategoryResponseDTO createCategory(CategoryRequestDTO categoryDTO);

    // Update an existing category
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryDTO);

    // Delete a category
    void deleteCategory(Long id);

    // Get category by ID
    CategoryResponseDTO getCategoryById(Long id);

    // Get all active categories
    List<CategoryResponseDTO> getActiveCategories();

    // Get category hierarchy
    List<CategoryResponseDTO> getCategoryTree();

    // Search categories with pagination
    PagedResponseDTO<CategoryResponseDTO> searchCategories(
            String name,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    // Update category status
    CategoryResponseDTO updateCategoryStatus(Long id, boolean active);

    // Get all categories
    List<CategoryResponseDTO> getAllCategories();
}
