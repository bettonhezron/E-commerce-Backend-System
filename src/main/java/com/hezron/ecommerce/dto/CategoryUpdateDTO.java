package com.hezron.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateDTO {

    @Schema(description = "ID of the category to update", example = "1")
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(description = "Name of the category", example = "Updated Electronics")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Updated description", example = "Updated description of category")
    private String description;

    @Schema(description = "Whether the category is active", example = "false")
    private Boolean active;

    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    @Schema(description = "Updated slug", example = "updated-electronics")
    private String slug;

    @Schema(description = "ID of the parent category, if changed", example = "null")
    private Long parentId;
}
