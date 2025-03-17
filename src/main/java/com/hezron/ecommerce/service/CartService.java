package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.model.User;


public interface CartService {

    CartDTO getCurrentCart();

    CartDTO addToCart(Long productId, Integer quantity);

    CartDTO updateCartItem(Long itemId, Integer quantity);

    CartDTO removeFromCart(Long itemId);

    void clearCart();

    void mergeGuestCartWithUserCart(String sessionId, User user);



}
