package com.example.ecommercedemo.dtos.carts.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCartItemDTO {

    private Long productId;
    private Integer quantity;

}
