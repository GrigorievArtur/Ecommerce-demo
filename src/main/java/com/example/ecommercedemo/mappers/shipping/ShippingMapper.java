package com.example.ecommercedemo.mappers.shipping;

import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.dtos.shipping.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipping.ShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipping.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.models.shipping.ShippingModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    ShippingModel toModel(ShippingPreset preset);
    ShippingPresetDTO toDTO(ShippingPreset preset);
    ShippingPreset toEntity(CreateShippingPresetDTO createShippingPresetDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePresetFromDto(UpdateShippingPresetDTO updateShippingPresetDTO, @MappingTarget ShippingPreset preset);


}
