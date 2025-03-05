package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CategoryDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Category;
import com.hezron.ecommerce.repository.CategoryRepository;
import com.hezron.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    // Mapper methods (you might want to use MapStruct for more complex mappings)
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive());
        
        // Set parent ID if parent exists
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }

        // Recursively convert subcategories
        if (category.getSubcategories() != null) {
            dto.setSubcategories(category.getSubcategories().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(dto.getActive() != null ? dto.getActive() : true);

        return category;
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = convertToEntity(categoryDTO);

        // Handle parent category if specified
        if (categoryDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Parent category not found: " + categoryDTO.getParentId()
                ));
            category.setParent(parentCategory);
        }

        return convertToDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        // Update parent category if specified
        if (categoryDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Parent category not found: " + categoryDTO.getParentId()
                ));
            existingCategory.setParent(parentCategory);
        }

        return convertToDTO(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        // Optional: Check if category has associated products before deletion
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return convertToDTO(category);
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getCategoryTree() {
        // Fetch root categories (no parent)
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        
        return rootCategories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public PagedResponseDTO<CategoryDTO> searchCategories(
        String name, 
        Boolean active, 
        int page, 
        int size, 
        String sortBy, 
        String sortDir
    ) {
        // Create sort object
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();

        // Create page request
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Perform search
        Page<Category> categoryPage = categoryRepository.searchCategories(name, active, pageRequest);

        // Convert to DTO
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        // Create paged response
        return new PagedResponseDTO<>(
            categoryDTOs,
            categoryPage.getNumber(),
            categoryPage.getSize(),
            categoryPage.getTotalElements(),
            categoryPage.getTotalPages(),
            categoryPage.isLast()
        );
    }

    @Override
    @Transactional
    public CategoryDTO updateCategoryStatus(Long id, boolean active) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        category.setActive(active);
        return convertToDTO(categoryRepository.save(category));
    }
}