package com.example.ecommercedemo.models.carts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemJson {
    private Long productId;
    private Integer quantity;
}