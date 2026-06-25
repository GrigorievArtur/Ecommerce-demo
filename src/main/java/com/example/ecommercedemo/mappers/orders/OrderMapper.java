package com.example.ecommercedemo.mappers.orders;

import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", ignore = true)
    OrderSnapshot toOrderSnapshot(Cart cart);
}
