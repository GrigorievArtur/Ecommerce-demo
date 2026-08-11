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
        ShippingPreset entity = saveShippingPresetEntity(createShippingPresetDTO, user);
        ShippingPresetDTO dto = shippingMapper.toDTO(entity);
        dto.setDefault(entity.isDefault());
        return dto;
    }

    public Page<ShippingPresetDTO> getShippingPresetPaged(Pageable pageable, User user) {
        return getShippingPresetPagedEntities(pageable, user).map(entity -> {
            ShippingPresetDTO dto = shippingMapper.toDTO(entity);
            dto.setDefault(entity.isDefault());
            return dto;
        });
    }

    public ShippingPresetDTO getShippingPreset(Long id, User user) {
        ShippingPreset entity = getShippingPresetEntity(id, user);
        ShippingPresetDTO dto = shippingMapper.toDTO(entity);
        dto.setDefault(entity.isDefault());
        return dto;
    }

    public ShippingPresetDTO updateShippingPreset(Long id, UpdateShippingPresetDTO dto, User user) {
        ShippingPreset entity = updateShippingPresetEntity(id, dto, user);
        ShippingPresetDTO result = shippingMapper.toDTO(entity);
        result.setDefault(entity.isDefault());
        return result;
    }

    // ----------------------------------------------------------
    // Entity methods (internal / reusable)
    // ----------------------------------------------------------

    //TODO : sa le mut in repo lol, si sa fac refactorin g la tot codu
    public ShippingPreset saveShippingPresetEntity(CreateShippingPresetDTO createShippingPresetDTO, User user) {
        ShippingPreset preset = shippingMapper.toEntity(createShippingPresetDTO);
        preset.setDefault(createShippingPresetDTO.getIsDefault());
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
        if (dto.getIsDefault() != null) {
            preset.setDefault(dto.getIsDefault());
        }
        return shippingPresetsRepo.save(preset);
    }

    public void deleteShippingPreset(Long id, User user) {
        shippingPresetsRepo.delete(getShippingPresetEntity(id, user));
    }
}