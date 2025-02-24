package com.hezron.ecommerce.controller;


import com.hezron.ecommerce.config.JwtService;
import com.hezron.ecommerce.dto.AuthResponse;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
import com.hezron.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO){
        UserDTO userDTO = userService.registerUser(registrationDTO);
        String  token = jwtService.generateToken(convertToUserDetails(userDTO));

        return ResponseEntity.ok(AuthResponse.builder()

                        .token(token)
                        .user(userDTO)
                        .build()


        );

    }

    private UserDetails convertToUserDetails(UserDTO userDTO) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(userDTO.getEmail())
                .password("")
                .authorities(new SimpleGrantedAuthority(userDTO.getRole().name()))
                .build();

    }
}
