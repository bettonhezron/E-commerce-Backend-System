package com.hezron.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponseDTO {

    @Schema(description = "Unique identifier of the category", example = "1")
    private Long id;

    @Schema(description = "Name of the category", example = "Electronics")
    private String name;

    @Schema(description = "Description of the category", example = "All electronic devices and gadgets")
    private String description;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean active;

    @Schema(description = "URL-friendly slug", example = "electronics")
    private String slug;

    @Schema(description = "ID of the parent category", example = "null")
    private Long parentId;

    @Schema(description = "List of subcategories")
    private List<CategoryResponseDTO> subcategories;

    @Schema(description = "Timestamp when the category was created", example = "2025-04-16T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the category was last updated", example = "2025-04-16T14:35:00")
    private LocalDateTime updatedAt;
}
