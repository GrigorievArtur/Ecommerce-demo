package com.example.ecommercedemo.dtos.carts;

import com.example.ecommercedemo.dtos.carts.items.CartItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private UUID suid;

    private BigDecimal totalPrice;

    private BigDecimal discountPercentage;

    private BigDecimal finalPrice;

    private List<CartItemDTO> items;


}
