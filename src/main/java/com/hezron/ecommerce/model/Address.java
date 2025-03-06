package com.hezron.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private String fullName;
    private String streetAddress;
    private String city;
    private String state;
    private  String zipCode;
    private String country;


    @Column
    private String phoneNumber;
    @Column(nullable = false)
    private boolean isDefault;

}
