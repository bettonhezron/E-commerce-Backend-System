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
    
    @Value("${admin.email:admin@ecommerce.com}")
    private String adminEmail;
    
    @Value("${admin.password:Admin@123}")
    private String adminPassword;
    
    @Value("${admin.firstName:Admin}")
    private String adminFirstName;
    
    @Value("${admin.lastName:User}")
    private String adminLastName;
    
    @Value("${admin.phoneNumber:+1234567890}")
    private String adminPhoneNumber;

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