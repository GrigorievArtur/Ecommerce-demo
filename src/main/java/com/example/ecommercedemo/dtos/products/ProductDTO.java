package com.example.ecommercedemo.dtos.products;

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
    private BigDecimal basePrice;
    private BigDecimal discountPercentage;
    private BigDecimal salePrice;
    private List<ProductMediaDTO> mediaList;
}

