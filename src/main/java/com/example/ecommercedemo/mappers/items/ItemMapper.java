package com.example.ecommercedemo.mappers.items;

import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.models.carts.ItemModel;
import com.example.ecommercedemo.services.products.ProductService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public abstract class ItemMapper {

    @Autowired
    protected ProductService productService;

    public abstract ItemModel toModel(CreateItemDTO cartItemDTO);

    public abstract ItemDTO toDTO(ItemModel itemModel);

    @AfterMapping
    protected void fillProductAndPrice(
            ItemModel itemModel,
            @MappingTarget ItemDTO itemDTO
            ) {
        ProductDTO product = productService.getCartItemProductDTO(itemModel);

        itemDTO.setProduct(product);

        BigDecimal quantity = BigDecimal.valueOf(itemDTO.getQuantity());
        BigDecimal itemSalePrice = product.getSalePrice().multiply(quantity);

        itemDTO.setSalePrice(itemSalePrice);
    }
}
