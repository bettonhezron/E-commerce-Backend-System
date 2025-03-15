package com.hezron.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
private Long id;
private String transactionID;
private Long orderId;
private BigDecimal amount;
private String currency;
private String paymentMethod;
private  String status;
private LocalDateTime createdAt;


}
