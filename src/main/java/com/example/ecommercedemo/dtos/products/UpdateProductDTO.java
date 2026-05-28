package com.example.ecommercedemo.dtos.products;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductDTO {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPercentage;
    private BigDecimal salePrice;
    private List<ProductMediaDTO> mediaList;
}
