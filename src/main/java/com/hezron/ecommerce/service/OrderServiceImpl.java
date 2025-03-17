package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderItemDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.*;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.ProductRepository;
import com.hezron.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    // List of supported payment methods - should match those in PaymentServiceImpl
    private static final List<String> SUPPORTED_PAYMENT_METHODS = Arrays.asList(
            "card", "paypal", "apple_pay", "google_pay", "bank_transfer"
    );

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
        // Validate payment method
        validatePaymentMethod(orderRequest.getPaymentMethod());

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
        order.setStatus(OrderStatus.PENDING);
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

            // Check product availability
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());

            // Reduce product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
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

        log.info("Order placed successfully: {}", savedOrder.getOrderNumber());

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

    @Transactional
    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        try {
            // Convert the string status to an enum
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);

            // If order is being shipped and no tracking number exists, generate one
            if (newStatus == OrderStatus.SHIPPED && order.getTrackingNumber() == null) {
                order.setTrackingNumber(generateTrackingNumber());
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        log.info("Order ID {} status updated to {}", orderId, order.getStatus().name());

        return convertToDTO(updatedOrder);
    }


    // Helper methods
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        // Example tax rate: 10%
        return subtotal.multiply(new BigDecimal("0.10"));
    }

    private BigDecimal calculateShippingCost(List<OrderItem> items) {
        int totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();
        BigDecimal baseRate = new BigDecimal("4.99");
        BigDecimal perItemRate = new BigDecimal("1.50");

        return baseRate.add(perItemRate.multiply(BigDecimal.valueOf(totalItems)));
    }


    // Validate payment method - should match the one in PaymentServiceImpl
    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
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
                .status(order.getStatus().name())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .trackingNumber(order.getTrackingNumber())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
}