package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCardId(Long cartId);
    void deleteByCartId(Long cartId);
}
