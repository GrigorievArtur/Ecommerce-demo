package com.example.ecommercedemo.mappers.items;

import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.models.items.ItemModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemModel toModel(CreateItemDTO cartItemDTO);



    ItemDTO toDTO(ItemModel itemModel);

}
