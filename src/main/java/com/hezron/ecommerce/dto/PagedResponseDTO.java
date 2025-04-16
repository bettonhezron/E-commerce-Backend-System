package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponseDTO<T> {

    private List<T> content;
    private int pageNumber;              // Current page number (0-based)
    private int pageSize;                // Number of items per page
    private long totalElements;          // Total number of items across all pages
    private int totalPages;              // Total number of pages
    private boolean first;               // Whether this is the first page
    private boolean last;                // Whether this is the last page
    private boolean empty;               // Whether the page is empty
    private String sortBy;               // Field used for sorting
    private String sortDirection;        // Sort direction (asc/desc)

    // Constructor that matches the usage in the previous example
    public PagedResponseDTO(List<T> content, int pageNumber, int pageSize,
                            long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = pageNumber == 0;
        this.last = last;
        this.empty = content == null || content.isEmpty();
    }

    //Factory method to create a PagedResponseDTO from Spring's Page object
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
        boolean empty = content == null || content.isEmpty();

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