package com.example.ecommercedemo.dtos.shipments;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateShippingPresetDTO {
    @NotBlank(message = "Preset name is required")
    private String presetName;

    @NotNull(message = "isDefault must be specified")
    private Boolean isDefault;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "{message.notnull}")
    private String phoneNumber;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Street is required")
    private String street;

    // building, apartment, postalCode can be optional
    private String building;
    private String apartment;

    private String postalCode;

}
