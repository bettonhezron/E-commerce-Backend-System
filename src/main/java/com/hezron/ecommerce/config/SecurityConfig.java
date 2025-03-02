package com.hezron.ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request

                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/products",
                                "/api/products/{id}",
                                "/api/products/search/advanced",
                                "/api/products/new-arrivals",
                                "/api/products/premium",
                                "/api/products/budget",
                                "/api/products/category/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/products/search-admin",
                                "/api/products/low-stock"
                        ).hasAnyRole("ADMIN")

                        // Customer-specific endpoints that require authentication
                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/**",
                                "/api/recommendations/**",
                                "/api/user/**"
                        ).authenticated()

                        // Restrict POST/PUT/DELETE operations on products to ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN")

                        // Any other request needs authentication
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
           provider.setUserDetailsService(userDetailsService);
           provider.setPasswordEncoder( new BCryptPasswordEncoder(12));
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config ) throws Exception {
       return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
