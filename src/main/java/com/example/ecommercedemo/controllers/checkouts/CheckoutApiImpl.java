package com.example.ecommercedemo.controllers.checkouts;

import com.example.ecommercedemo.api.checkout.CheckoutApi;
import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.checkouts.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Transactional
public class CheckoutApiImpl implements CheckoutApi {

    @Autowired
    private CheckoutService checkoutService;

    @Override
    public ResponseEntity<OrderDTO> checkoutDefaultShippingPreset(UUID suid, User user) {
        return ResponseEntity.ok(checkoutService.checkoutDefault(suid, user));
    }

    @Override
    public ResponseEntity<OrderDTO> checkoutSavedShippingPreset(UUID suid, User user, Long shippingPresetId) {
        return ResponseEntity.ok(checkoutService.checkoutSaved(suid, user, shippingPresetId));
    }

    @Override
    public ResponseEntity<OrderDTO> checkoutNewShippingPreset(UUID suid, User user, CreateShippingPresetDTO shippingPresetDTO) {
        return ResponseEntity.ok(checkoutService.checkoutNew(suid, user, shippingPresetDTO));
    }
}
