package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ProductDTO;
import com.hezron.ecommerce.service.CartService;
import com.hezron.ecommerce.service.OrderService;
import com.hezron.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    
    private final ProductService productService;
  //  private final CartService cartService;  // You'll need to implement this
   // private final OrderService orderService;  // You'll need to implement this
    
    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductDTO>> getRecommendations() {
        // Implement personalized recommendations based on user history
        return ResponseEntity.ok(List.of());
    }
    
    @PostMapping("/cart/add/{productId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Add to cart logic using authenticated user information
        return ResponseEntity.ok().build();
    }

    /*
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        // Create order logic
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

     */
}