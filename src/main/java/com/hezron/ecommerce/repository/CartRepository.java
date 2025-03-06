package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Cart;
import com.hezron.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findBySessionId(String sessionId);
}
