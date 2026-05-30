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
public class CreateCartItemDTO {

    private Long productId;

    private Integer quantity;

}
