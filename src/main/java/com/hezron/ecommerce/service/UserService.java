package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.LoginDTO;
import com.hezron.ecommerce.dto.UserDTO;
import com.hezron.ecommerce.dto.UserRegistrationDTO;

public interface UserService {
    UserDTO registerUser(UserRegistrationDTO registrationDTO);
    UserDTO getUserById(Long id);
    UserDTO authenticateUser(LoginDTO loginDTO);
    UserDTO getUserByEmail(String email);

}
