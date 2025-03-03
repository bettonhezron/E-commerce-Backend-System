package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for standardized response format
 * @param <T> The type of data in the response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    
    private boolean success;     // Indicates if the operation was successful
    private String message;      // Response message
    private T data;              // Response payload
    
    /**
     * Constructor for success responses with data
     * 
     * @param message Success message
     * @param data Response payload
     */
    public ApiResponseDTO(String message, T data) {
        this(true, message, data);
    }
    
    /**
     * Constructor for error responses without data
     * 
     * @param message Error message
     */
    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>(false, message, null);
    }
    
    /**
     * Constructor for success responses without data
     * 
     * @param message Success message
     */
    public static <T> ApiResponseDTO<T> success(String message) {
        return new ApiResponseDTO<>(true, message, null);
    }
}