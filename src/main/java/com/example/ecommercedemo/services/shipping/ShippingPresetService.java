package com.example.ecommercedemo.services.shipping;

import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.ShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.shipping.ShippingMapper;
import com.example.ecommercedemo.repositories.shipping.ShippingPresetsRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ShippingPresetService {

    @Autowired
    private ShippingPresetsRepo shippingPresetsRepo;

    @Autowired
    private ShippingMapper shippingMapper;

    // ----------------------------------------------------------
    // DTO methods (public API)
    // ----------------------------------------------------------

    public ShippingPresetDTO saveShippingPreset(CreateShippingPresetDTO createShippingPresetDTO, User user) {
        return shippingMapper.toDTO(saveShippingPresetEntity(createShippingPresetDTO, user));
    }

    public Page<ShippingPresetDTO> getShippingPresetPaged(Pageable pageable, User user) {
        return getShippingPresetPagedEntities(pageable, user).map(shippingMapper::toDTO);
    }

    public ShippingPresetDTO getShippingPreset(Long id, User user) {
        return shippingMapper.toDTO(getShippingPresetEntity(id, user));
    }

    public ShippingPresetDTO updateShippingPreset(Long id, UpdateShippingPresetDTO dto, User user) {
        return shippingMapper.toDTO(updateShippingPresetEntity(id, dto, user));
    }

    // ----------------------------------------------------------
    // Entity methods (internal / reusable)
    // ----------------------------------------------------------

    public ShippingPreset saveShippingPresetEntity(CreateShippingPresetDTO createShippingPresetDTO, User user) {
        ShippingPreset preset = shippingMapper.toEntity(createShippingPresetDTO);
        preset.setUser(user);
        return shippingPresetsRepo.save(preset);
    }

    public Page<ShippingPreset> getShippingPresetPagedEntities(Pageable pageable, User user) {
        return shippingPresetsRepo.findByUser(user, pageable);
    }

    public ShippingPreset getDefaultShippingPresetEntity(User user) {
        return shippingPresetsRepo.findByIsDefaultAndUser(true, user).orElseThrow(EntityNotFoundException::new);
    }

    public ShippingPreset getShippingPresetEntity(Long id, User user) {
        return shippingPresetsRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipping preset not found or not owned by user"));
    }

    public ShippingPreset updateShippingPresetEntity(Long id, UpdateShippingPresetDTO dto, User user) {
        ShippingPreset preset = getShippingPresetEntity(id, user);
        shippingMapper.updatePresetFromDto(dto, preset);
        return shippingPresetsRepo.save(preset);
    }

    public void deleteShippingPreset(Long id, User user) {
        shippingPresetsRepo.delete(getShippingPresetEntity(id, user));
    }

}