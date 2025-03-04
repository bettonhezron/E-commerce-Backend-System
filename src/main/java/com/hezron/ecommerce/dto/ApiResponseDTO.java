package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    
    private boolean success;     // Indicates if the operation was successful
    private String message;      // Response message
    private T data;              // Response payload
    
     public ApiResponseDTO(String message, T data) {
        this(true, message, data);
    }
    

    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>(false, message, null);
    }
    
      public static <T> ApiResponseDTO<T> success(String message) {
        return new ApiResponseDTO<>(true, message, null);
    }
}