package com.hezron.ecommerce.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderRequest {
    // Getters and Setters
    private double amount;
    private ShippingInfo shipping;
    private PaymentInfo payment;
    private List<CartItem> items;

    // Inner classes for nested objects
    @Setter
    @Getter
    public static class ShippingInfo {
        // Getters and Setters
        private String firstName;
        private String lastName;
        private String email;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private boolean savedAddress;

    }

    @Setter
    @Getter
    public static class PaymentInfo {
        // Getters and Setters
        private String type;
        private String paymentMethodId;
        private String nameOnCard;

    }

    @Setter
    @Getter
    public static class CartItem {
        // Getters and Setters
        private String id;
        private String name;
        private String image;
        private String specs;
        private int quantity;
        private double price;

    }
}