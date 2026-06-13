package com.example.ecommercedemo.models.pricing.frozen;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FrozenUnitPrice {

    @NotNull
    @Embedded
    private FrozenPrice frozenPrice;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Digits(integer = 3, fraction = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    public BigDecimal effectivePrice() {
        // Short-circuit: Always return the true historical price without double discounting
        if (frozenPrice == null) return BigDecimal.ZERO;
        return frozenPrice.effectivePrice();
    }
}