package com.example.ecommercedemo.mappers.shipping;

import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.ShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.models.shipping.ShippingModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    ShippingModel toModel(ShippingPreset preset);
    ShippingPresetDTO toDTO(ShippingPreset preset);
    ShippingPreset toEntity(CreateShippingPresetDTO createShippingPresetDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePresetFromDto(UpdateShippingPresetDTO updateShippingPresetDTO, @MappingTarget ShippingPreset preset);


}
