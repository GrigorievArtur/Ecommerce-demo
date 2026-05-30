package com.example.ecommercedemo.mappers.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDTO cartToCartDTO(Cart cart);

}
