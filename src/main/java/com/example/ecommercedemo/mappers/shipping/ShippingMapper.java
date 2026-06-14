package com.example.ecommercedemo.mappers.shipping;

import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.models.shipping.ShippingModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    ShippingModel toModel(ShippingPreset preset);

}
