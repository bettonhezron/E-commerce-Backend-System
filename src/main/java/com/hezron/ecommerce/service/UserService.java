package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
import com.hezron.ecommerce.exception.EmailAlreadyExistsException;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Role;
import com.hezron.ecommerce.model.User;
import com.hezron.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO registerUser(UserRegistrationDTO registrationDTO){
        if(userRepository.existsByEmail(registrationDTO.getEmail())){
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setRole(Role.ROLE_CUSTOMER);

        User savedUser = userRepository.save(user);
       return convertDTO(savedUser);

    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertDTO(user);
    }

    //Login (Authenticate User)
    @Transactional(readOnly = true)
    public UserDTO authenticateUser(Login)


    private UserDTO convertDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }

}
