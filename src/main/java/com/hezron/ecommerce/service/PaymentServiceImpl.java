package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import com.hezron.ecommerce.exception.PaymentProcessingException;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService{
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secretKey}")
    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {
         String username = SecurityContextHolder.getContext().getAuthentication().getName();

         Order order = orderRepository.findById(paymentRequest.getOrderId())
                 .orElseThrow(() -> new ResourceNotFoundException("Oder not found with ID: " + paymentRequest.getOrderId()));
         //Ensure the order belongs to the current user
         if(!order.getUser().getEmail().equals(username)){
             throw new AccessDeniedException("You don't have permission to process payment for this order");
         }

         //Check if order already has a payment
        if(paymentRepository.findByOrder(order).isPresent()){
            throw  new PaymentProcessingException("Payment already exists for this order");
        }
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
