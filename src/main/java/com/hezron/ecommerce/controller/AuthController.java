package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.config.JwtService;
import com.hezron.ecommerce.dto.AuthResponse;
import com.hezron.ecommerce.dto.LoginDTO;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
import com.hezron.ecommerce.model.Role;
import com.hezron.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        // By default, new registrations are CUSTOMER role
        registrationDTO.setRole(Role.CUSTOMER);
        UserDTO userDTO = userService.registerUser(registrationDTO);
        String token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build()
        );
    }

    // Admin registration - should be secured or have a special code/key
    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody UserRegistrationDTO registrationDTO,
            @RequestParam String adminKey) {

        // Verify the admin registration key
        if (!"your-secure-admin-key".equals(adminKey)) {
            return ResponseEntity.status(403).build();
        }

        // Set the role to ADMIN
        registrationDTO.setRole(Role.ADMIN);
        UserDTO userDTO = userService.registerUser(registrationDTO);
        String token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build()
        );
    }

    // Login user
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDTO userDTO = userService.authenticateUser(loginDTO);
        String token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build());
    }

    private UserDetails convertToUserDetails(UserDTO userDTO) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(userDTO.getEmail())
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_" + userDTO.getRole().name()))
                .build();
    }
}