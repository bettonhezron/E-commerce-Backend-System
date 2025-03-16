package com.hezron.ecommerce.controller;

import com.hezron.ecommerce.dto.PaymentDTO;
import com.hezron.ecommerce.dto.PaymentRequestDTO;
import com.hezron.ecommerce.dto.PaymentResponseDTO;
import com.hezron.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "APIs for managing payments" )
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process payment for an order")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully!"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO paymentRequest) throws AccessDeniedException {
        log.info("Process payment for order ID: {}", paymentRequest.getOrderId());
        return new ResponseEntity<>(paymentService.processPayment(paymentRequest), HttpStatus.CREATED);
    }

//payment details by order ID
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details by oder ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment  details retrieved successfully!"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentDTO> getPaymentByTransactionId (
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable String transactionId) throws AccessDeniedException {
        log.info("Retrieving payment for transaction ID: {}", transactionId);
        return ResponseEntity.ok(paymentService.getPaymentByTransactionId(transactionId));
    }

    //Webhooks
    @PostMapping("/webhooks")
    @Operation(summary = "Handle stripe webhook events")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "403", description = "Invalid webhook payload or signature")
    })
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {
        log.info("Received Stripe Webhook");
        paymentService.handleStripeWebHook(payload, signatureHeader);
        return ResponseEntity.ok("Webhook processed successfully");
    }
}
