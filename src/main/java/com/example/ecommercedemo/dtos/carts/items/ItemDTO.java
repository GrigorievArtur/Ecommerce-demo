package com.example.ecommercedemo.dtos.carts.items;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
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
    private PriceSnapshot priceSnapshot;
}
