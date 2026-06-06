package com.example.ecommercedemo.mappers.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.services.carts.CartService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface CartMapper {

    CartDTO cartToCartDTO(Cart cart);
}
