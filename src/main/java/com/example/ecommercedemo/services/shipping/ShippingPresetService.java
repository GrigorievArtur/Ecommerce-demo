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

    public ShippingPresetDTO saveShippingPreset(CreateShippingPresetDTO createShippingPresetDTO, User user) {
        ShippingPreset preset = shippingMapper.toEntity(createShippingPresetDTO);
        preset.setUser(user);
        return shippingMapper.toDTO(shippingPresetsRepo.save(preset));
    }

    public Page<ShippingPresetDTO> getShippingPresetPaged(Pageable pageable, User user) {
        var page = shippingPresetsRepo.findByUser(user, pageable);
        return page.map(shippingMapper::toDTO);
    }

    public ShippingPresetDTO getShippingPreset(Long id, User user) {
        return shippingMapper.toDTO(
                shippingPresetsRepo.findByIdAndUser(id, user)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Shipping preset not found or not owned by user"))
        );
    }

    public ShippingPresetDTO updateShippingPreset(Long id, UpdateShippingPresetDTO dto, User user) {
        ShippingPreset preset = shippingPresetsRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipping preset not found or not owned by user"));
        shippingMapper.updatePresetFromDto(dto, preset);
        return shippingMapper.toDTO(shippingPresetsRepo.save(preset));
    }

    public void deleteShippingPreset(Long id, User user) {
        int deletedCount = shippingPresetsRepo.deleteByUserAndId(user, id);
        if (deletedCount == 0) {
            throw new EntityNotFoundException("Shipping preset not found or not owned by user");
        }
    }





}