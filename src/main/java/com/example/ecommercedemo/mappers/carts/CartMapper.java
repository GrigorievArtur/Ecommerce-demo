package com.example.ecommercedemo.mappers.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.services.carts.CartService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface CartMapper {

    @Mapping(target = "items", ignore = true)
    CartDTO cartToCartDTO(Cart cart);
}
