package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private Long orderId;
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private  String cardCvv;
    private String cardHolderName;
}
