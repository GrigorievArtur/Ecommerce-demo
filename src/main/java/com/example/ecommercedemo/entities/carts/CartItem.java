package com.example.ecommercedemo.entities.carts;

import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Long productId;

    @Builder.Default
    private PriceSnapshot priceSnapshot = new PriceSnapshot();

}
