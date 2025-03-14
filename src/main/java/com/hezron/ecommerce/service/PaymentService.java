package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO);
    PaymentDTO getPaymentByOrderId(Long orderId);
    PaymentDTO getPaymentByTransactionId(String transactionId);

}
