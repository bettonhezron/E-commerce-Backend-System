package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService{
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
/*
    @Value("${stripe.api.key}")
    private String stipeApiKey;

    @Value("${app.currency")
    private String defaultCurrency;

    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {
       String username = SecurityContextHolder.getContext().getAuthentication().getName();

       Order order = orderRepository.findById(paymentRequestDTO.getOrderId() );

        return null;
    }
    */

    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {
        return null;
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        return null;
    }

    @Override
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        return null;
    }
}
