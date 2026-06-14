package com.example.ecommercedemo.entities.shipments;

import com.example.ecommercedemo.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shipping_presets")
public class ShippingPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g., "Home", "Work", "Mom's House"
    private String presetName;
    private boolean isDefault;

    private String fullName;
    private String email;
    private String phoneNumber;

    private String country;
    private String state;
    private String city;
    private String street;
    private String building;
    private String apartment;
    private String postalCode;

    // Many presets belong to One User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}