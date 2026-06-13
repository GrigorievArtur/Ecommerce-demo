package com.example.ecommercedemo.models.orders;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentData {

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
}