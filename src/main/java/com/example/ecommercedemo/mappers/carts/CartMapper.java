package com.example.ecommercedemo.mappers.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface CartMapper {

    CartDTO cartToCartDTO(Cart cart);


    @AfterMapping
    default void calculatePrice(@MappingTarget CartDTO dto) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (dto.getItems() != null) {
            for (ItemDTO item : dto.getItems()) {
                if (item.getSalePrice() != null) {
                    totalPrice = totalPrice.add(item.getSalePrice());
                }
            }
        }

        BigDecimal discountPercentage = dto.getDiscountPercentage() == null
                ? BigDecimal.ZERO
                : dto.getDiscountPercentage();

        BigDecimal discountAmount = totalPrice
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100));

        dto.setTotalPrice(totalPrice);
        dto.setFinalPrice(totalPrice.subtract(discountAmount));
    }
}
