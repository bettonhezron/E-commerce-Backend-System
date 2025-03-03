package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for paginated responses
 * Contains content items, pagination metadata, and sorting information
 * @param <T> The type of items in the content list
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    private List<T> content;             // List of items in the current page
    private int pageNumber;              // Current page number (0-based)
    private int pageSize;                // Number of items per page
    private long totalElements;          // Total number of items across all pages
    private int totalPages;              // Total number of pages
    private boolean first;               // Whether this is the first page
    private boolean last;                // Whether this is the last page
    private boolean empty;               // Whether the page is empty
    private String sortBy;               // Field used for sorting
    private String sortDirection;        // Sort direction (asc/desc)

    public PagedResponseDTO(List<ProductDTO> productDTOs, int number, int size, long totalElements, int totalPages, boolean last) {
    }

    /**
     * Factory method to create a PagedResponseDTO from Spring's Page object
     * 
     * @param content List of items for the current page
     * @param pageNumber Current page number (0-based)
     * @param pageSize Size of each page
     * @param totalElements Total number of items across all pages
     * @param totalPages Total number of pages
     * @param sortBy Field used for sorting
     * @param sortDirection Sort direction (asc/desc)
     * @return A new PagedResponseDTO instance
     */
    public static <T> PagedResponseDTO<T> of(
            List<T> content, 
            int pageNumber, 
            int pageSize, 
            long totalElements, 
            int totalPages,
            String sortBy, 
            String sortDirection) {
        
        boolean first = pageNumber == 0;
        boolean last = pageNumber == totalPages - 1 || totalPages == 0;
        boolean empty = content.isEmpty();
        
        return PagedResponseDTO.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(first)
                .last(last)
                .empty(empty)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}