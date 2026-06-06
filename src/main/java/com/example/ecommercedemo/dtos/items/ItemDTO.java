package com.example.ecommercedemo.dtos.items;

import com.example.ecommercedemo.dtos.products.ProductDTO;
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
    private int quantity;
    private BigDecimal basePrice;
    private BigDecimal totalPrice;

}
