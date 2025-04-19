package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.dto.CartItemDTO;
import com.hezron.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart", description = "Retrieves the current user's shopping cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "Cart is empty")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<CartDTO>> getCart() {
        log.debug("Fetching current cart");
        CartDTO cart = cartService.getCurrentCart();
        
        if (cart.getItems().isEmpty()) {
            log.info("Cart is empty");
            return ResponseEntity.noContent().build();
        }
        
        log.info("Retrieved cart with {} items", cart.getItems().size());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Cart retrieved successfully", 
                cart
        ));
    }

    @Operation(summary = "Add product to cart", description = "Adds a product to the current user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity")
    })
    @PostMapping("/items")
    public ResponseEntity<ApiResponseDTO<CartDTO>> addToCart(
            @Parameter(description = "Cart item details", required = true)
            @RequestBody CartItemDTO cartItemDTO) {
        log.debug("Adding product ID: {} with quantity: {} to cart", 
                cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        
        CartDTO updatedCart = cartService.addToCart(cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        
        log.info("Product added to cart successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Product added to cart successfully", 
                updatedCart
        ));
    }

    @Operation(summary = "Update cart item", description = "Updates the quantity of a product in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseDTO<CartDTO>> updateCartItem(
            @Parameter(description = "Cart item ID", required = true)
            @PathVariable Long itemId,
            
            @Parameter(description = "New quantity", required = true)
            @RequestParam int quantity) {
        log.debug("Updating cart item ID: {} with quantity: {}", itemId, quantity);
        
        CartDTO updatedCart = cartService.updateCartItem(itemId, quantity);
        
        log.info("Cart item updated successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Cart item updated successfully", 
                updatedCart
        ));
    }

    @Operation(summary = "Remove item from cart", description = "Removes an item from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseDTO<CartDTO>> removeFromCart(
            @Parameter(description = "Cart item ID", required = true)
            @PathVariable Long itemId) {
        log.debug("Removing item ID: {} from cart", itemId);
        
        CartDTO updatedCart = cartService.removeFromCart(itemId);
        
        log.info("Item removed from cart successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Item removed from cart successfully", 
                updatedCart
        ));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    @DeleteMapping
    public ResponseEntity<ApiResponseDTO<Void>> clearCart() {
        log.debug("Clearing all items from cart");
        
        cartService.clearCart();
        
        log.info("Cart cleared successfully");
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Cart cleared successfully", 
                null
        ));
    }
}