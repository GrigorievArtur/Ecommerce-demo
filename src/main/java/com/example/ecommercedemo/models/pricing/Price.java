package com.example.ecommercedemo.models.pricing;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder(toBuilder = true)
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    /** Base unit price before any discounts */
    @Builder.Default
    private BigDecimal grossPrice = BigDecimal.ZERO;

    /** Line total: grossPrice * quantity (set by PriceService) */
    @Builder.Default
    private BigDecimal grossAmount = BigDecimal.ZERO;

    /** Flat discount amount (e.g. coupon) */
    @Builder.Default
    private BigDecimal grossDiscount = BigDecimal.ZERO;

    /** Percentage discount (0-100), applied after flat discount */
    @Builder.Default
    private BigDecimal percentageDiscount = BigDecimal.ZERO;

    /** Item quantity */
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;
}
