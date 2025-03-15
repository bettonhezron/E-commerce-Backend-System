package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) throws AccessDeniedException;
    PaymentDTO getPaymentByOrderId(Long orderId) throws AccessDeniedException;
    PaymentDTO getPaymentByTransactionId(String transactionId) throws AccessDeniedException;

}
