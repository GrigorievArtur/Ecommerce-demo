package com.example.ecommercedemo.filters.products;

import com.example.ecommercedemo.enums.products.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilter {
    private String query;
    private Category category;
}
