package com.hezron.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Schema(description = "Unique identifier of the category", example = "1")
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(description = "Name of the category", example = "Electronics")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the category", example = "All electronic devices and gadgets")
    private String description;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean active = true;

    @Schema(description = "ID of the parent category", example = "null")
    private Long parentId;

    @Schema(description = "URL-friendly version of the category name", example = "electronics")
    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    private String slug;

    @Schema(description = "List of subcategories")
    private List<CategoryDTO> subcategories;
}