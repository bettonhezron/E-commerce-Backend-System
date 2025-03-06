package com.hezron.ecommerce.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String shippingAddress;
    private String billingAddress;
    private  String trackingNumber;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}
