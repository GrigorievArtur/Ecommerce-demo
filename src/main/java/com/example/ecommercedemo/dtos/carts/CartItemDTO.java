package com.example.ecommercedemo.dtos.carts;

import com.example.ecommercedemo.dtos.products.ProductMediaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

    private Long productId;

    private String productName;

    private BigDecimal basePrice;

    private BigDecimal productDiscountPercentage;

    private BigDecimal finalUnitPrice;

    private Integer quantity;

    private ProductMediaDTO thumbnail;

    private BigDecimal lineTotal;
}