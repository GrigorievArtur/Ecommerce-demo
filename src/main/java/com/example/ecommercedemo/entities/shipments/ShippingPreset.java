package com.example.ecommercedemo.entities.shipments;

import com.example.ecommercedemo.entities.users.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties("user")
public class ShippingPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO : ADD VALIDATION

    // e.g., "Home", "Work", "Mom's House"
    private String presetName;

    @JsonProperty("default")
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