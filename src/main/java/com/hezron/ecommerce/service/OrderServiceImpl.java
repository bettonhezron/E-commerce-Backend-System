package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.CartDTO;
import com.hezron.ecommerce.dto.OrderDTO;
import com.hezron.ecommerce.dto.OrderItemDTO;
import com.hezron.ecommerce.dto.OrderRequestDTO;

import com.hezron.ecommerce.exception.ResourceNotFoundException;

import com.hezron.ecommerce.model.*;
import com.hezron.ecommerce.repository.OrderItemRepository;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.ProductRepository;
import com.hezron.ecommerce.repository.AddressRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final UserService userService;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderRequestDTO orderRequest) {
        // Get current user
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("You must be logged in to place an order"));

        // Get current cart
        CartDTO cart = cartService.getCurrentCart();

        // Validate cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot place an order with an empty cart");
        }

        // Validate and get shipping address
        Address shippingAddress = null;
        if (orderRequest.getShippingAddressId() != null) {
            shippingAddress = addressRepository.findByIdAndUser(orderRequest.getShippingAddressId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));
        } else if (orderRequest.getShippingAddress() == null || orderRequest.getShippingAddress().isBlank()) {
            throw new ValidationException("Shipping address is required");
        }

        // Get billing address if provided
        Address billingAddress = null;
        if (orderRequest.getBillingAddressId() != null) {
            billingAddress = addressRepository.findByIdAndUser(orderRequest.getBillingAddressId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Billing address not found"));
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING.name());
        order.setSubtotal(cart.getSubtotal());
        order.setTax(cart.getTax());
        order.setShippingCost(cart.getShippingCost());
        order.setTotal(cart.getTotal());

        // Set shipping address
        if (shippingAddress != null) {
            order.setShippingAddress(formatAddress(shippingAddress));
        } else {
            order.setShippingAddress(orderRequest.getShippingAddress());
        }

        // Set billing address
        if (billingAddress != null) {
            order.setBillingAddress(formatAddress(billingAddress));
        } else if (orderRequest.getBillingAddress() != null && !orderRequest.getBillingAddress().isBlank()) {
            order.setBillingAddress(orderRequest.getBillingAddress());
        } else if (shippingAddress != null) {
            order.setBillingAddress(formatAddress(shippingAddress));
        } else {
            order.setBillingAddress(orderRequest.getShippingAddress());
        }

        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setItems(new ArrayList<>());

        // Save the order first to get an ID
        order = orderRepository.save(order);

        // Create order items and update product stock
        for (var cartItem : cart.getItems()) {
            // Get the product to update stock
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + cartItem.getProductId()));

            // Check if product is still in stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ValidationException("Product '" + product.getName() +
                        "' does not have enough stock. Available: " + product.getStockQuantity());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());

            // Add to order
            order.getItems().add(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Save order items
        order.getItems().forEach(orderItemRepository::save);

        // Clear the cart
        cartService.clearCart();

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        // Get current user
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("You must be logged in to view orders"));

        // Find order
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(user.getId()) && !userService.isAdmin()) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return mapToOrderDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getCurrentUserOrders() {
        // Get current user
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("You must be logged in to view orders"));

        // Get user's orders
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);

        return orders.stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Formats an address object into a string
     */
    private String formatAddress(Address address) {
        return String.format("%s, %s, %s, %s %s, %s",
                address.getFullName(),
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry());
    }

    /**
     * Generates a unique order number
     */
    private String generateOrderNumber() {
        // Format: ORD-{UUID last 8 chars}-{Timestamp}
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
        return "ORD-" + uuid + "-" + timestamp;
    }

    /**
     * Maps an Order entity to OrderDTO
     */
    private OrderDTO mapToOrderDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .totalAmount(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentMethod(order.getPaymentMethod())
                .items(itemDTOs)
                .build();
    }

    /**
     * Maps an OrderItem entity to OrderItemDTO
     */
    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}