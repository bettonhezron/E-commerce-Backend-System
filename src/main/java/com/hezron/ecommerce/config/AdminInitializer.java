package com.hezron.ecommerce.config;

import com.hezron.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;
    
    @Value("${ADMIN_EMAIL:admin@ecommerce.com}")
    private String adminEmail;
    
    @Value("${ADMIN_EMAIL:Admin@456}")
    private String adminPassword;
    
    @Value("${ADMIN_FIRSTNAME:Admin}")
    private String adminFirstName;
    
    @Value("${ADMIN_FIRSTNAME:User}")
    private String adminLastName;
    
    @Value("${ADMIN_PHONENUMBER:+1234567890}")
    private String adminPhoneNumber;

    @Value("${ADMIN_KEY:y2BcE8fL9pQ7rT3sX5vZ1mD4nG6hJ8kA}")
    private String adminKey;

    @Override
    public void run(String... args) {
        // Create an initial admin user if one doesn't exist
        userService.createInitialAdminIfNeeded(
                adminEmail, 
                adminPassword, 
                adminFirstName, 
                adminLastName,
                adminPhoneNumber
        );
    }
}