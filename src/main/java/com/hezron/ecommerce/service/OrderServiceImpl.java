package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderItemDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.model.OrderItem;
import com.hezron.ecommerce.model.Product;
import com.hezron.ecommerce.model.User;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.ProductRepository;
import com.hezron.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderRequestDTO orderRequest) {
        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return placeOrder(orderRequest, username);
    }

    @Transactional
    @Override
    public OrderDTO placeOrder(OrderRequestDTO orderRequest, String username) {
        // Get the user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        // Get the current cart
        CartDTO cartDTO = cartService.getCurrentCart();

        if (cartDTO.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart");
        }

        // Create the order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setBillingAddress(orderRequest.getBillingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setCreatedAt(LocalDateTime.now());

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var cartItem : cartDTO.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItem.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        // Set order amounts
        order.setSubtotal(subtotal);
        order.setTax(calculateTax(subtotal));
        order.setShippingCost(calculateShippingCost(orderItems));
        order.setTotalAmount(order.getSubtotal().add(order.getTax()).add(order.getShippingCost()));

        // Set order items
        order.setItems(orderItems);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after successful order placement
        cartService.clearCart();

        return convertToDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getOrderById(id, username);
    }

    @Override
    public OrderDTO getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        // Security check: ensure the order belongs to the current user
        if (!order.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }

        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getCurrentUserOrders() {
        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getCurrentUserOrders(username);
    }

    @Override
    public List<OrderDTO> getCurrentUserOrders(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        // Example tax rate: 10%
        return subtotal.multiply(new BigDecimal("0.10"));
    }

    private BigDecimal calculateShippingCost(List<OrderItem> items) {
        // Simple shipping cost calculation based on item count
        int totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();

        if (totalItems <= 2) {
            return new BigDecimal("5.99");
        } else if (totalItems <= 5) {
            return new BigDecimal("8.99");
        } else {
            return new BigDecimal("12.99");
        }
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .trackingNumber(order.getTrackingNumber())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}