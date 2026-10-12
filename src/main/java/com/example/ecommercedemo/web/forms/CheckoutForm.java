package com.example.ecommercedemo.web.forms;

import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.payments.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Shipping + payment fields for the checkout screen. Payment details are processed by the
 * configured {@code PaymentGateway} and never persisted.
 */
@Data
public class CheckoutForm {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "State / county is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Street address is required")
    private String street;

    private String building;
    private String apartment;
    private String postalCode;

    private boolean savePreset = true;
    private String presetName = "Home";

    private PaymentMethod method = PaymentMethod.CARD;
    private String cardNumber;
    private String cardHolder;
    private String expiry;
    private String cvc;

    /** Template helper — avoids enum introspection in FreeMarker. */
    public boolean isCardSelected() {
        return method == null || method == PaymentMethod.CARD;
    }

    public boolean isWalletSelected() {
        return method == PaymentMethod.WALLET;
    }

    public CreateShippingPresetDTO toShippingPreset() {
        String name = (presetName == null || presetName.isBlank()) ? "Checkout address" : presetName.trim();
        return CreateShippingPresetDTO.builder()
                .presetName(name)
                .isDefault(false)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phoneNumber)
                .country(country)
                .state(state)
                .city(city)
                .street(street)
                .building(building)
                .apartment(apartment)
                .postalCode(postalCode)
                .build();
    }
}
