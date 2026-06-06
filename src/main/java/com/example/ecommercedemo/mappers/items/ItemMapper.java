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
public interface ItemMapper {

    ItemModel toModel(CreateItemDTO cartItemDTO);

    ItemDTO toDTO(ItemModel itemModel);

}
