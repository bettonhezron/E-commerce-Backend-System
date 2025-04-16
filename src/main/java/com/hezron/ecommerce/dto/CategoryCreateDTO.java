package com.hezron.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(description = "Name of the category", example = "Electronics")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the category", example = "All electronic devices and gadgets")
    private String description;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean active = true;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    @Schema(description = "URL-friendly slug", example = "electronics")
    private String slug;

    @Schema(description = "ID of the parent category, if this is a subcategory", example = "3")
    private Long parentId;
}
