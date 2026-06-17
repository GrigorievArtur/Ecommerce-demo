package com.example.ecommercedemo.mappers.orders;

import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderSnapshot toOrderSnapshot(Cart cart);
}
