package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentDetails;
    private String specialInstructions;
}
