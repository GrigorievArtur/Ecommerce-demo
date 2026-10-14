package com.example.ecommercedemo.web.forms;

import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Backing bean for the "saved addresses" section of the account page.
 */
@Data
public class AddressForm {

    @NotBlank(message = "Give this address a label")
    @Size(max = 60, message = "Label must be 60 characters or fewer")
    private String presetName = "Home";

    private Boolean isDefault = Boolean.FALSE;

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

    public CreateShippingPresetDTO toCreateDTO() {
        return CreateShippingPresetDTO.builder()
                .presetName(presetName)
                .isDefault(Boolean.TRUE.equals(isDefault))
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

    public UpdateShippingPresetDTO toUpdateDTO() {
        UpdateShippingPresetDTO dto = new UpdateShippingPresetDTO();
        dto.setPresetName(presetName);
        dto.setIsDefault(Boolean.TRUE.equals(isDefault));
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setCountry(country);
        dto.setState(state);
        dto.setCity(city);
        dto.setStreet(street);
        dto.setBuilding(building);
        dto.setApartment(apartment);
        dto.setPostalCode(postalCode);
        return dto;
    }

    public static AddressForm from(ShippingPreset preset) {
        AddressForm form = new AddressForm();
        form.setPresetName(preset.getPresetName());
        form.setIsDefault(preset.isDefault());
        form.setFullName(preset.getFullName());
        form.setEmail(preset.getEmail());
        form.setPhoneNumber(preset.getPhoneNumber());
        form.setCountry(preset.getCountry());
        form.setState(preset.getState());
        form.setCity(preset.getCity());
        form.setStreet(preset.getStreet());
        form.setBuilding(preset.getBuilding());
        form.setApartment(preset.getApartment());
        form.setPostalCode(preset.getPostalCode());
        return form;
    }
}
