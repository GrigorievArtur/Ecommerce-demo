package com.example.ecommercedemo.controllers.checkouts;

import com.example.ecommercedemo.api.checkout.CheckoutApi;
import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.checkouts.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Transactional
@RequiredArgsConstructor
public class CheckoutApiImpl implements CheckoutApi {

    private final CheckoutService checkoutService;

    @Override
    public ResponseEntity<OrderDTO> checkoutDefaultShippingPreset(UUID suid, User user) {
        return ResponseEntity.ok(checkoutService.checkoutDefault(CartOwner.of(user, suid)));
    }

    @Override
    public ResponseEntity<OrderDTO> checkoutSavedShippingPreset(UUID suid, User user, Long shippingPresetId) {
        return ResponseEntity.ok(checkoutService.checkoutSaved(CartOwner.of(user, suid), shippingPresetId));
    }

    @Override
    public ResponseEntity<OrderDTO> checkoutNewShippingPreset(UUID suid, User user, CreateShippingPresetDTO shippingPresetDTO) {
        return ResponseEntity.ok(checkoutService.checkoutNew(CartOwner.of(user, suid), shippingPresetDTO));
    }
}
