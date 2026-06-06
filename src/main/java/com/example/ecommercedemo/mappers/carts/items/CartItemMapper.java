package com.example.ecommercedemo.mappers.carts.items;

import com.example.ecommercedemo.dtos.carts.items.CartItemDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateCartItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.models.carts.CartItemModel;
import com.example.ecommercedemo.services.products.ProductService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public abstract class CartItemMapper {

    @Autowired
    protected ProductService productService;

    public abstract CartItemModel toModel(CreateCartItemDTO cartItemDTO);

    public abstract CartItemDTO toDTO(CartItemModel cartItemModel);

    @AfterMapping
    protected void fillProductAndPrice(
            CartItemModel cartItemModel,
            @MappingTarget CartItemDTO cartItemDTO
            ) {
        ProductDTO product = productService.getCartItemProductDTO(cartItemModel);

        cartItemDTO.setProduct(product);

        BigDecimal quantity = BigDecimal.valueOf(cartItemDTO.getQuantity());
        BigDecimal itemSalePrice = product.getSalePrice().multiply(quantity);

        cartItemDTO.setSalePrice(itemSalePrice);
    }
}
