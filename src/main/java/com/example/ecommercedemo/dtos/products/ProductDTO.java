package com.example.ecommercedemo.dtos.products;


import com.example.ecommercedemo.models.pricing.Price;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String name;
    private String description;
    private Price price;
    private List<ProductMediaDTO> mediaList;
}

