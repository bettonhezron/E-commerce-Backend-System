package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.ApiResponseDTO;
import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;
import com.hezron.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling order operations
 * Provides endpoints for managing orders
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order", description = "Creates a new order from the current cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or empty cart"),
            @ApiResponse(responseCode = "403", description = "User not authenticated")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> placeOrder(
            @Parameter(description = "Order request details", required = true)
            @Valid @RequestBody OrderRequestDTO orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("Creating new order for user: {}", userDetails.getUsername());

        OrderDTO newOrder = orderService.placeOrder(orderRequest, userDetails.getUsername());

        log.info("Order placed successfully with order number: {}", newOrder.getOrderNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(
                "Order placed successfully",
                newOrder
        ));
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("Fetching order with ID: {} for user: {}", id, userDetails.getUsername());

        OrderDTO order = orderService.getOrderById(id, userDetails.getUsername());

        log.info("Retrieved order with ID: {}", id);
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Order retrieved successfully",
                order
        ));
    }

    @Operation(summary = "Get current user's orders", description = "Retrieves all orders of the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No orders found"),
            @ApiResponse(responseCode = "403", description = "User not authenticated")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getCurrentUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("Fetching orders for user: {}", userDetails.getUsername());

        List<OrderDTO> orders = orderService.getCurrentUserOrders(userDetails.getUsername());

        if (orders.isEmpty()) {
            log.info("No orders found for user: {}", userDetails.getUsername());
            return ResponseEntity.noContent().build();
        }

        log.info("Retrieved {} orders for user: {}", orders.size(), userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>(
                "Orders retrieved successfully",
                orders
        ));
    }
}