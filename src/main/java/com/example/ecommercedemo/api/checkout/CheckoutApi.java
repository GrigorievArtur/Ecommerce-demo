package com.example.ecommercedemo.api.checkout;

import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("api/checkout")
@Tag(name = "Checkout logic for users")
public interface CheckoutApi {

    @PostMapping("/default")
    @Operation(summary = "Checkouts the current user/guest cart with the default shipping address")
    ResponseEntity<OrderDTO> checkoutDefaultShippingPreset(
            @CookieValue(name = "suid", required = false) UUID suid,
            @AuthenticationPrincipal User user
    );

    @PostMapping("/saved/{shippingPresetId}")
    @Operation(summary = "Checkouts the current user/guest cart with a saved shipping address preset")
    ResponseEntity<OrderDTO> checkoutSavedShippingPreset(
            @CookieValue(name = "suid", required = false) UUID suid,
            @AuthenticationPrincipal User user,
            @PathVariable Long shippingPresetId
    );

    @PostMapping("/new")
    @Operation(summary = "Checkouts the current user/guest cart with a new shipping address")
    ResponseEntity<OrderDTO> checkoutNewShippingPreset(
            @CookieValue(name = "suid", required = false) UUID suid,
            @AuthenticationPrincipal User user,
            @RequestBody CreateShippingPresetDTO shippingPresetDTO
    );
}
