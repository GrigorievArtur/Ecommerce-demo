package com.example.ecommercedemo.api.shipping;

import com.example.ecommercedemo.dtos.shipping.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipping.ShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipping.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/shipping")
@Tag(name = "Shipping")
public interface ShippingAPI {

    @Operation(summary = "Saves shipping information")
    @PostMapping
    ResponseEntity<ShippingPresetDTO> saveShippingPreset(
            @AuthenticationPrincipal User user,
            @RequestBody CreateShippingPresetDTO createShippingPresetDTO);

    @Operation(summary = "Updates shipping information")
    @PatchMapping
    ResponseEntity<ShippingPresetDTO> updateShippingPreset(
            @RequestParam Long id,
            @AuthenticationPrincipal User user,
            @RequestBody UpdateShippingPresetDTO updateShippingPresetDTO);

    @Operation(summary = "Gets shipping information")
    @GetMapping
    ResponseEntity<ShippingPresetDTO> getShippingPreset(
            @AuthenticationPrincipal User user,
            @RequestParam Long id);

    @Operation(summary = "Gets shipping presets with pagination")
    @GetMapping("/paged")
    ResponseEntity<Page<ShippingPresetDTO>> getShippingPresetPaged(
            @AuthenticationPrincipal User user,
            @ParameterObject Pageable pageable);

    @Operation(summary = "Deletes shipping information")
    @DeleteMapping
    ResponseEntity<Void> deleteShippingPreset(
            @AuthenticationPrincipal User user,
            @RequestParam Long id);
}