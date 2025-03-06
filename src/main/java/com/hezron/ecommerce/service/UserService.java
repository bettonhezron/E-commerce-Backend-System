package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.LoginDTO;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;
import com.hezron.ecommerce.model.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(UserRegistrationDTO registrationDTO);
    UserDTO registerAdminUser(UserRegistrationDTO registrationDTO, String adminKey);
    UserDTO getUserById(Long id);
    UserDTO authenticateUser(LoginDTO loginDTO);
    UserDTO getUserByEmail(String email);
    void createInitialAdminIfNeeded(String email, String password, String firstName, String lastName, String phoneNumber);

    //Get the currently authenticated user

    Optional<User> getCurrentUser();

    boolean isAdmin();


}