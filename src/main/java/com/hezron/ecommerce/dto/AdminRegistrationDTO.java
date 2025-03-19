package com.hezron.ecommerce.dto;


import lombok.Data;

@Data

public class AdminRegistrationDTO {
    private UserRegistrationDTO registrationDTO;
    private String adminKey;
}
