package com.example.ecommercedemo.entities.products;

import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.products.ProductMedia;
import jakarta.persistence.*;
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
@Entity
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPercentage;
    private BigDecimal salePrice;
    private Category category;

    @ElementCollection
    @CollectionTable(name = "product_media", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductMedia> mediaList;
}
