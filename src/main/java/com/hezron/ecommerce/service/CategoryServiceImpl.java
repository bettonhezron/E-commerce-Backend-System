package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CategoryRequestDTO;
import com.hezron.ecommerce.dto.CategoryResponseDTO;
import com.hezron.ecommerce.dto.PagedResponseDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Category;
import com.hezron.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // Mapper methods
    private CategoryResponseDTO convertToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive());
        dto.setSlug(category.getSlug());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }

        if (category.getSubcategories() != null) {
            List<CategoryResponseDTO> subcategoryDtos = category.getSubcategories().stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            dto.setSubcategories(subcategoryDtos);
        }

        return dto;
    }

    private Category convertToEntity(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(dto.getActive() != null ? dto.getActive() : true);
        String slug = dto.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        category.setSlug(slug);

        return category;
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryDTO) {
        Category category = convertToEntity(categoryDTO);

        if (categoryDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found: " + categoryDTO.getParentId()
                    ));
            category.setParent(parentCategory);
        }

        return convertToResponseDTO(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        if (categoryDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found: " + categoryDTO.getParentId()
                    ));
            existingCategory.setParent(parentCategory);
        }

        return convertToResponseDTO(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return convertToResponseDTO(category);
    }

    @Override
    public List<CategoryResponseDTO> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrue();
        return categories.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponseDTO<CategoryResponseDTO> searchCategories(
            String name,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Category> categoryPage = categoryRepository.searchCategories(name, active, pageRequest);

        List<CategoryResponseDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

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
    public CategoryResponseDTO updateCategoryStatus(Long id, boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        category.setActive(active);
        return convertToResponseDTO(categoryRepository.save(category));
    }
}
