package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.LoginDTO;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
import com.hezron.ecommerce.exception.EmailAlreadyExistsException;
import com.hezron.ecommerce.exception.InvalidCredentialsException;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Role;
import com.hezron.ecommerce.model.User;
import com.hezron.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.key}")
    private String expectedAdminKey;

    // Register user
    @Override
    @Transactional
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());

        // Set role based on DTO if provided, otherwise default to CUSTOMER
        user.setRole(registrationDTO.getRole() != null ? registrationDTO.getRole() : Role.CUSTOMER);

        User savedUser = userRepository.save(user);
        return convertDTO(savedUser);
    }

    // Create admin user method
    @Override
    @Transactional
    public UserDTO registerAdminUser(UserRegistrationDTO registrationDTO, String adminKey) {
        // Verify admin key (store this in a secure environment variable in production)
        if (!expectedAdminKey.equals(adminKey)) {
            throw new InvalidCredentialsException("Invalid admin registration key");
        }

        // Set role to ADMIN explicitly
        registrationDTO.setRole(Role.ADMIN);
        return registerUser(registrationDTO);
    }

    // Create initial admin if needed
    @Override
    @Transactional
    public void createInitialAdminIfNeeded(String email, String password, String firstName, String lastName, String phoneNumber) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User adminUser = new User();
            adminUser.setEmail(email);
            adminUser.setPassword(passwordEncoder.encode(password));
            adminUser.setFirstName(firstName);
            adminUser.setLastName(lastName);
            adminUser.setPhoneNumber(phoneNumber);
            adminUser.setRole(Role.ADMIN);

            userRepository.save(adminUser);
        }
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")){
            return Optional.empty();
        }
        String username = authentication.getName();
        return userRepository.findByEmail(username);
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.isAuthenticated()){
            return false;
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertDTO(user);
    }

    // Login User
    @Override
    @Transactional(readOnly = true)
    public UserDTO authenticateUser(LoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return convertDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertDTO(user);
    }


    @Override
    public String getGuestSessionId() {
        return UUID.randomUUID().toString();
    }


    // Converts User entity to UserDTO to return only essential details
    private UserDTO convertDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        return dto;
    }
}