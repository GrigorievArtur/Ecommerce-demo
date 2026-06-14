package com.example.ecommercedemo.dtos.items;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {

    private ProductDTO product;
    private FrozenLinePrice frozenLinePrice;
}
