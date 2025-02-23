package com.hezron.ecommerce.dto;

import com.hezron.ecommerce.model.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private BigDecimal totalAmount;
    private String status;
    private Long shippingAddressId;
    private  String trackingNumber;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}
