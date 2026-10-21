package com.example.ecommercedemo.dtos.shipments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingPresetDTO {

    private Long id;

    private String presetName;
    @JsonProperty("isDefault")
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
}