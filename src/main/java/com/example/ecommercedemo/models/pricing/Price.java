package com.example.ecommercedemo.models.pricing;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    private BigDecimal grossPrice;// base price
    private BigDecimal grossAmount;// base price * qty inclus discountable

    private BigDecimal grossDiscount;
    private BigDecimal percentageDiscount;
    private BigDecimal quantity;


}
