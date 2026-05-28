package com.example.ecommercedemo.dtos.products;

import java.math.BigDecimal;
import java.util.List;

public class CreateProductDTO {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPercentage;
    private BigDecimal salePrice;
    private List<ProductMediaDTO> mediaList;
}
