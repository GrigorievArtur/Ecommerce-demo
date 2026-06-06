package com.example.ecommercedemo.dtos.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateItemDTO {

    private Long productId;
    private Integer quantity;

}
