package com.example.ecommercedemo.controllers.shipping;

import com.example.ecommercedemo.api.shipping.ShippingAPI;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.ShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class ShippingApiImpl implements ShippingAPI {

    @Autowired
    private ShippingPresetService shippingPresetService;

    @Override
    public ResponseEntity<ShippingPresetDTO> saveShippingPreset(User user, CreateShippingPresetDTO createShippingPresetDTO) {
        return ResponseEntity.ok(shippingPresetService.saveShippingPreset(createShippingPresetDTO, user));
    }

    @Override
    public ResponseEntity<ShippingPresetDTO> updateShippingPreset(Long id, User user, UpdateShippingPresetDTO updateShippingPresetDTO) {
        return ResponseEntity.ok(shippingPresetService.updateShippingPreset(id, updateShippingPresetDTO, user));
    }

    @Override
    public ResponseEntity<ShippingPresetDTO> getShippingPreset(User user, Long id) {
        return ResponseEntity.ok(shippingPresetService.getShippingPreset(id, user));
    }

    @Override
    public ResponseEntity<Page<ShippingPresetDTO>> getShippingPresetPaged(User user, Pageable pageable) {
        return ResponseEntity.ok(shippingPresetService.getShippingPresetPaged(pageable, user));
    }

    @Override
    public ResponseEntity<Void> deleteShippingPreset(User user, Long id) {
        shippingPresetService.deleteShippingPreset(id, user);
        return ResponseEntity.noContent().build();
    }
}
