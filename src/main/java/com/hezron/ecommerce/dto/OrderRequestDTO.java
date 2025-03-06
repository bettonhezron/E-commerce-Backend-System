package com.hezron.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Long shippingAddressId;
    private String shippingAddress;

    private Long  billingAddressId;
    private String billingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String paymentDetails;
    private String specialInstructions;
}
