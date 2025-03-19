package com.hezron.ecommerce.config;

import com.hezron.ecommerce.model.User;
import com.hezron.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Add the ROLE_ prefix to conform to Spring Security's expected format
        String role = "ROLE_" + user.getRole().name();
        log.debug("Adding roles to JWT: {}", role);



        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // username (using email instead)
                user.getPassword(),        // password
                user.isActive(),           // enabled
                true,                      // accountNonExpired
                true,                      // credentialsNonExpired
                true,                      // accountNonLocked
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }
}