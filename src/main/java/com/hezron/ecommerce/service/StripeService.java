package com.hezron.ecommerce.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * Creates a payment intent in Stripe
     *
     * @param amount The payment amount
     * @param currency The currency code
     * @param description Description of the payment
     * @param orderId The associated order ID
     * @param customerEmail Customer's email for receipt
     * @return PaymentIntent object from Stripe
     * @throws StripeException If there's an error communicating with Stripe
     */
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, 
                                            String description, String orderId, 
                                            String customerEmail) throws StripeException {
        // Initialize Stripe
        Stripe.apiKey = stripeApiKey;

        // Create a unique idempotency key for this payment attempt
        String idempotencyKey = UUID.randomUUID().toString();

        // Convert amount from BigDecimal to long (cents)
        long amountInCents = amount.movePointRight(2).longValue();

        // Create payment intent params
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setDescription(description)
                .putMetadata("order_id", orderId)
                .setReceiptEmail(customerEmail)
                .build();

        // Create request options with idempotency key
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();

        // Create payment intent with options
        return PaymentIntent.create(params, requestOptions);
    }

    /**
     * Retrieves a payment intent from Stripe
     * 
     * @param paymentIntentId The payment intent ID
     * @return PaymentIntent object from Stripe
     * @throws StripeException If there's an error communicating with Stripe
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        // Initialize Stripe
        Stripe.apiKey = stripeApiKey;
        
        // Retrieve the payment intent
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * Cancels a payment intent in Stripe
     * 
     * @param paymentIntentId The payment intent ID
     * @return The canceled PaymentIntent
     * @throws StripeException If there's an error communicating with Stripe
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        // Initialize Stripe
        Stripe.apiKey = stripeApiKey;
        
        // Retrieve the payment intent
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        // Cancel the payment intent
        return paymentIntent.cancel();
    }
}