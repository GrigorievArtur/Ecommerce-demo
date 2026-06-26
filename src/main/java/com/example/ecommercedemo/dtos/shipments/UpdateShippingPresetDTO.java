package com.example.ecommercedemo.dtos.shipments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateShippingPresetDTO {
    private String presetName;

    private Boolean isDefault;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String country;

    private String state;

    private String city;

    private String street;

    // building, apartment, postalCode can be optional
    private String building;
    private String apartment;

    private String postalCode;

}
