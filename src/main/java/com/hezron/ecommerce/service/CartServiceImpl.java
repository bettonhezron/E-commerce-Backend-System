package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.dto.CartItemDTO;

import com.hezron.ecommerce.exception.ResourceNotFoundException;

import com.hezron.ecommerce.model.Cart;
import com.hezron.ecommerce.model.CartItem;
import com.hezron.ecommerce.model.Product;
import com.hezron.ecommerce.model.User;
import com.hezron.ecommerce.repository.CartItemRepository;
import com.hezron.ecommerce.repository.CartRepository;
import com.hezron.ecommerce.repository.ProductRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;
    
    // Tax rate - could be moved to configuration
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    
    // Free shipping threshold
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    
    // Standard shipping cost
    private static final BigDecimal STANDARD_SHIPPING_COST = new BigDecimal("5.99");

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCurrentCart() {
        Cart cart = getOrCreateCart();
        return mapToCartDTO(cart);
    }
    @Override
    @Transactional
    public CartDTO addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }

        // Get the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Ensure the product is available
        if (!product.getActive()) {
            throw new ValidationException("Product is not available");
        }
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Not enough stock available.");
        }

        // Get or create cart (handles both guests and authenticated users)
        Cart cart = getOrCreateCart();

        // Add or update item in cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new ValidationException("Not enough stock available for the requested quantity");
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(quantity)));

            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToCartDTO(cart);
    }


    @Override
    @Transactional
    public CartDTO updateCartItem(Long itemId, Integer quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
        
        // Get cart item
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        // Check if item belongs to current user's cart
        Cart userCart = getOrCreateCart();
        if (!cartItem.getCart().getId().equals(userCart.getId())) {
            throw new ResourceNotFoundException("Cart item not found with ID: " + itemId);
        }
        
        // Check stock availability
        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Not enough stock available. Currently available: " + product.getStockQuantity());
        }
        
        // Update quantity
        cartItem.setQuantity(quantity);

        //update total price
        cartItem.setTotalPrice(cartItem.getUnitPrice().multiply(new BigDecimal(quantity)));
        cartItemRepository.save(cartItem);
        
        // Update cart totals
        updateCartTotals(userCart);
        cartRepository.save(userCart);
        
        return mapToCartDTO(userCart);
    }

    @Override
    @Transactional
    public CartDTO removeFromCart(Long itemId) {
        // Get cart item
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        // Check if item belongs to current user's cart
        Cart userCart = getOrCreateCart();
        if (!cartItem.getCart().getId().equals(userCart.getId())) {
            throw new ResourceNotFoundException("Cart item not found with ID: " + itemId);
        }
        
        // Remove item
        userCart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        // Update cart totals
        updateCartTotals(userCart);
        cartRepository.save(userCart);
        
        return mapToCartDTO(userCart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        
        // Delete all items
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        
        // Reset totals
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setTax(BigDecimal.ZERO);
        cart.setShippingCost(BigDecimal.ZERO);
        cart.setTotalAmount(BigDecimal.ZERO);
        
        cartRepository.save(cart);
    }
    
    /**
     * Gets current user's cart or creates a new one if it doesn't exist
     */
    private Cart getOrCreateCart() {
        Optional<User> currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            // Authenticated user
            User user = currentUser.get();
            return cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        newCart.setItems(new ArrayList<>());
                        newCart.setSubtotal(BigDecimal.ZERO);
                        newCart.setTax(BigDecimal.ZERO);
                        newCart.setShippingCost(BigDecimal.ZERO);
                        newCart.setTotalAmount(BigDecimal.ZERO);

                        return cartRepository.save(newCart);
                    });
        } else {
            // Guest user - retrieve session ID
            String sessionId = userService.getGuestSessionId();

            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setSessionId(sessionId);
                        newCart.setItems(new ArrayList<>());

                        // Initialize the BigDecimal fields
                        newCart.setSubtotal(BigDecimal.ZERO);
                        newCart.setTax(BigDecimal.ZERO);
                        newCart.setShippingCost(BigDecimal.ZERO);
                        newCart.setTotalAmount(BigDecimal.ZERO);

                        return cartRepository.save(newCart);
                    });
        }
    }



    /**
     * Updates the cart totals (subtotal, tax, shipping, total)
     */
    private void updateCartTotals(Cart cart) {
        // Calculate subtotal
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Set subtotal
        cart.setSubtotal(subtotal);
        
        // Calculate tax
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        cart.setTax(tax);
        
        // Calculate shipping cost
        BigDecimal shippingCost = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? 
                BigDecimal.ZERO : STANDARD_SHIPPING_COST;
        cart.setShippingCost(shippingCost);
        
        // Calculate total
        BigDecimal total = subtotal.add(tax).add(shippingCost);
        cart.setTotalAmount(total);
    }
    
    /**
     * Maps a Cart entity to CartDTO
     */
    private CartDTO mapToCartDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());
        
        return CartDTO.builder()
                .items(itemDTOs)
                .subtotal(cart.getSubtotal())
                .tax(cart.getTax())
                .shippingCost(cart.getShippingCost())
                .total(cart.getTotalAmount())
                .itemCount(cart.getItems().size())
                .build();
    }

    @Transactional
    public void mergeGuestCartWithUserCart(String sessionId, User user) {
        // Find guest cart by session ID
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);

        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            // No guest cart or empty cart, nothing to merge
            return;
        }

        Cart guestCart = guestCartOpt.get();

        // Find or create user cart
        Cart userCart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    newCart.setSubtotal(BigDecimal.ZERO);
                    newCart.setTax(BigDecimal.ZERO);
                    newCart.setShippingCost(BigDecimal.ZERO);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        // Move items from guest cart to user cart
        for (CartItem item : new ArrayList<>(guestCart.getItems())) {
            // Check if product already exists in user cart
            Optional<CartItem> existingItemOpt = userCart.getItems().stream()
                    .filter(userItem -> userItem.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                // Update quantity of existing item
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                existingItem.setTotalPrice(existingItem.getUnitPrice()
                        .multiply(new BigDecimal(existingItem.getQuantity())));
                cartItemRepository.save(existingItem);

                // Remove the item from guest cart
                guestCart.getItems().remove(item);
                cartItemRepository.delete(item);
            } else {
                // Move item to user cart
                item.setCart(userCart);
                userCart.getItems().add(item);
            }
        }

        // Update cart totals
        updateCartTotals(userCart);

        // Save changes
        cartRepository.save(userCart);

        // Delete guest cart
        cartRepository.delete(guestCart);
    }
    
    /**
     * Maps a CartItem entity to CartItemDTO
     */
    private CartItemDTO mapToCartItemDTO(CartItem item) {
        BigDecimal totalPrice = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));

        // Get the first image URL from the list, or null if the list is empty
        String imageUrl = null;
        if (item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty()) {
            imageUrl = item.getProduct().getImageUrls().getFirst();
        }

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(imageUrl) // Use the first image from the list
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }
}