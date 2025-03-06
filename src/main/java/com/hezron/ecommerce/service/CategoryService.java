package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CategoryDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;


import java.util.List;

public interface CategoryService {
    // Create a new category
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    // Update an existing category
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    // Delete a category
    void deleteCategory(Long id);

    // Get category by ID
    CategoryDTO getCategoryById(Long id);

    // Get all active categories
    List<CategoryDTO> getActiveCategories();

    // Get category hierarchy
    List<CategoryDTO> getCategoryTree();

    // Search categories with pagination
    PagedResponseDTO<CategoryDTO> searchCategories(
        String name, 
        Boolean active, 
        int page, 
        int size, 
        String sortBy, 
        String sortDir
    );

    // Update category status
    CategoryDTO updateCategoryStatus(Long id, boolean active);
}