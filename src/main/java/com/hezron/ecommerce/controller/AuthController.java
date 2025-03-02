package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.config.JwtService;
import com.hezron.ecommerce.dto.AuthResponse;
import com.hezron.ecommerce.dto.LoginDTO;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Operation(summary = "Register a new customer user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserDTO userDTO = userService.registerUser(registrationDTO);
        String token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build()
        );
    }

    @Operation(summary = "Register an admin user", description = "Requires a secure admin key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered admin"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Invalid admin key"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody UserRegistrationDTO registrationDTO,
            @RequestParam String adminKey) {

        UserDTO userDTO = userService.registerAdminUser(registrationDTO, adminKey);
        String token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

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