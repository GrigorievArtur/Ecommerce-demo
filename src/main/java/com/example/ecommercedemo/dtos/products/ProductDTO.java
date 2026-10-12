package com.example.ecommercedemo.dtos.products;


import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.Price;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Category category;
    private Integer stock;
    private String description;
    private Price price;
    private List<ProductMediaDTO> mediaList;

    /** True when the product is available for purchase. Template-only (not part of the JSON API). */
    @JsonIgnore
    public boolean isAvailable() {
        return stock == null || stock > 0;
    }

    /** Template-friendly category name (avoids enum introspection in views). */
    @JsonIgnore
    public String getCategoryName() {
        return category == null ? null : category.name();
    }

    @JsonIgnore
    public String getCategoryLabel() {
        return category == null ? null : category.getLabel();
    }
}

