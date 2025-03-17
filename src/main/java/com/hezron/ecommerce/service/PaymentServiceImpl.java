package com.hezron.ecommerce.service;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import com.hezron.ecommerce.exception.PaymentProcessingException;
import com.hezron.ecommerce.exception.ResourceNotFoundException;
import com.hezron.ecommerce.model.Order;
import com.hezron.ecommerce.model.OrderStatus;
import com.hezron.ecommerce.model.Payment;
import com.hezron.ecommerce.repository.OrderRepository;
import com.hezron.ecommerce.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.currency}")
    private String defaultCurrency;

    // List of supported payment methods
    private static final List<String> SUPPORTED_PAYMENT_METHODS = Arrays.asList(
            "card", "paypal", "apple_pay", "google_pay", "bank_transfer"
    );

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) throws AccessDeniedException {
        // Validate payment method
        validatePaymentMethod(paymentRequest.getPaymentMethod());

        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch the order
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + paymentRequest.getOrderId()));

        // Ensure the order belongs to the current user
        if (!order.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You don't have permission to process payment for this order");
        }

        // Check if order already has a payment
        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new PaymentProcessingException("Payment already exists for this order");
        }

        try {
            // Delegate Stripe payment creation to StripeService
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    order.getTotalAmount(),
                    defaultCurrency,
                    "Order #" + order.getOrderNumber(),
                    order.getId().toString(),
                    order.getUser().getEmail()
            );

            // Update order status
            order.setStatus(OrderStatus.PAYMENT_PROCESSING);
            orderRepository.save(order);

            // Create payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .transactionId(paymentIntent.getId())
                    .amount(order.getTotalAmount())
                    .currency(defaultCurrency)
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .status("PENDING")
                    .gatewayResponse(paymentIntent.toJson())
                    .createdAt(LocalDateTime.now())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            // Return response with client secret for frontend to complete payment
            return PaymentResponseDTO.builder()
                    .transactionId(savedPayment.getTransactionId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status("PENDING")
                    .message("Payment initiated successfully")
                    .payment(mapToDTO(savedPayment))
                    .build();

        } catch (StripeException e) {
            log.error("Stripe payment processing error", e);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) throws AccessDeniedException {
        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with the ID: " + orderId));

        // Ensure the order belongs to the current user
        if (!order.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You don't have permission to access this payment");
        }

        // Get payment
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));

        return mapToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByTransactionId(String transactionId) throws AccessDeniedException {
        // Get current authenticated username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Get payment
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));

        if (!payment.getOrder().getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You do not have permission to access this payment.");
        }

        return mapToDTO(payment);
    }

    @Override
    public void handleStripeWebHook(String payload, String signatureHeader) {

    }

    @Override
    @Transactional
    public PaymentDTO cancelPayment(String transactionId) throws AccessDeniedException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //Get payment
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));
        // Ensure the payment belongs to the current user
        if (!payment.getOrder().getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You don't have permission to cancel this payment");
        }

        // Check if payment can be canceled (only pending payments can be canceled)
        if (!"PENDING".equals(payment.getStatus())) {
            throw new PaymentProcessingException("Payment cannot be canceled because it's already " + payment.getStatus());
        }

        try {
            // Cancel the payment intent in Stripe
            PaymentIntent canceledIntent = stripeService.cancelPaymentIntent(payment.getTransactionId());

            // Update payment status
            payment.setStatus("CANCELED");
            payment.setGatewayResponse(canceledIntent.toJson());
            payment.setUpdatedAt(LocalDateTime.now());
            Payment updatedPayment = paymentRepository.save(payment);

            // Update order status
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAYMENT_CANCELED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.info("Payment {} canceled successfully", transactionId);

            return mapToDTO(updatedPayment);
        } catch (StripeException e) {
            log.error("Error canceling payment in Stripe", e);
            throw new PaymentProcessingException("Failed to cancel payment: " + e.getMessage());
        }

    }

    // Complete Webhook handler to process stripe events
    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        try {
            // Initialize stripe
            Stripe.apiKey = stripeApiKey;

            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

            // Deserialize the event data
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

            log.info("Processing Stripe webhook event: {}", event.getType());

            if (!dataObjectDeserializer.getObject().isPresent()) {
                log.warn("Unable to deserialize event data: {}", event.getId());
                return;
            }

            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            throw new PaymentProcessingException("Invalid webhook signature: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing stripe webhook", e);
            throw new PaymentProcessingException("Failed to process webhook: " + e.getMessage());
        }
    }

    // Helper method to handle payment_intent.succeeded events
    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
        String paymentIntentId = paymentIntent.getId();

        // Find and update the payment record
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + paymentIntentId));

        payment.setStatus("COMPLETED");
        payment.setGatewayResponse(paymentIntent.toJson());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update the order status
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("Payment completed successfully for order: {}", order.getId());
    }

    // Helper method to handle payment_intent.payment_failed events
    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
        String paymentIntentId = paymentIntent.getId();

        // Find and update the payment record
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + paymentIntentId));

        payment.setStatus("FAILED");
        payment.setGatewayResponse(paymentIntent.toJson());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update the order status
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        log.error("Payment failed for order: {}", order.getId());
    }

    // Helper method to handle payment_intent.canceled events
    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
        String paymentIntentId = paymentIntent.getId();

        // Find and update the payment record
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + paymentIntentId));

        payment.setStatus("CANCELED");
        payment.setGatewayResponse(paymentIntent.toJson());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update the order status
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAYMENT_CANCELED);
        orderRepository.save(order);

        log.info("Payment canceled for order: {}", order.getId());
    }

    // Validate payment method
    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.toLowerCase())) {
            throw new PaymentProcessingException("Unsupported payment method: " + paymentMethod);
        }
    }

    // Helper method to map payment to PaymentDTO
    private PaymentDTO mapToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId()) // Changed from transactionID to transactionId for consistency
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}