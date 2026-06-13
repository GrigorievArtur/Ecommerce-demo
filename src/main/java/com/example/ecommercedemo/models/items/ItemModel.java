package com.example.ecommercedemo.models.items;

import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemModel {

    private Long productId;

    private FrozenLinePrice frozenLinePrice;

}