package com.hezron.ecommerce.dto;

import com.hezron.ecommerce.model.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;

    //no password for security
}
