package com.example.ecommercedemo.models.pricing;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BasePrice {

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

}