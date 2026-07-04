package com.example.ecommercedemo.dtos.orders;

import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
import com.example.ecommercedemo.dtos.shipments.ShippingPresetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    // May be null for guest orders
    private Long userId;

    private ShippingPresetDTO shippingPreset;

    @Builder.Default
    private List<ItemDTO> items = new ArrayList<>();

}
