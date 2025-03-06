package com.hezron.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    //for anonymous carts
    @Column(name = "session_id")
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL,orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CartItem> items;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax", precision = 10, scale = 2, nullable = false)
    private BigDecimal tax;

    @Column(name = "shipping_cost", precision = 10, scale = 2, nullable = false)
    private BigDecimal shippingCost;

    @Column(name = "totalAmount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}