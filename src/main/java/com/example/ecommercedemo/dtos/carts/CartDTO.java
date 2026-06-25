package com.example.ecommercedemo.dtos.carts;

import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
import com.example.ecommercedemo.models.pricing.Price;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private UUID suid;

    private Price price;

    private List<ItemDTO> items;


}
