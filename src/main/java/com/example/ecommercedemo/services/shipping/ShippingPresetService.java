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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ShippingPresetService {

    private final ShippingPresetsRepo shippingPresetsRepo;
    private final ShippingMapper shippingMapper;

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

    /** Builds an unsaved preset (used for guest checkout, where no user owns an address). */
    public ShippingPreset toEntity(CreateShippingPresetDTO dto) {
        return shippingMapper.toEntity(dto);
    }

    public ShippingPreset saveShippingPresetEntity(CreateShippingPresetDTO createShippingPresetDTO, User user) {
        ShippingPreset preset = shippingMapper.toEntity(createShippingPresetDTO);
        preset.setDefault(createShippingPresetDTO.getIsDefault());
        preset.setUser(user);
        if (preset.isDefault()) {
            clearOtherDefaults(user, null);
        }
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
        if (preset.isDefault()) {
            clearOtherDefaults(user, preset.getId());
        }
        return shippingPresetsRepo.save(preset);
    }

    public void deleteShippingPreset(Long id, User user) {
        shippingPresetsRepo.delete(getShippingPresetEntity(id, user));
    }

    /**
     * A user can only have one default address. Running this before persisting avoids a
     * second default, which would make {@code findByIsDefaultAndUser} ambiguous.
     */
    private void clearOtherDefaults(User user, Long keepId) {
        for (ShippingPreset preset : shippingPresetsRepo.findByUser(user)) {
            boolean isAnotherDefault = preset.isDefault()
                    && (keepId == null || !keepId.equals(preset.getId()));
            if (isAnotherDefault) {
                preset.setDefault(false);
                shippingPresetsRepo.save(preset);
            }
        }
    }
}
