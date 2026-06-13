package com.example.ecommercedemo.models.pricing.frozen;

import com.example.ecommercedemo.models.pricing.LinePrice;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.Min;
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
public class FrozenLinePrice {

    @Min(1)
    @Builder.Default
    private int quantity = 1;

    @NotNull
    @Embedded
    private FrozenUnitPrice frozenUnitPrice;

    // Standard constructor factory method to create from live LinePrice
    public static FrozenLinePrice from(LinePrice live) {
        if (live == null) return new FrozenLinePrice();
        return FrozenLinePrice.builder()
                .quantity(live.getQuantity())
                .frozenUnitPrice(FrozenUnitPrice.builder()
                        .frozenPrice(FrozenPrice.freeze(live.getUnitPrice()))
                        .discountPercentage(live.getUnitPrice().getDiscountPercentage())
                        .build())
                .build();
    }

    public BigDecimal effectivePrice() {
        if (frozenUnitPrice == null) return BigDecimal.ZERO;
        // Correctly multiplies item count by the verified historical single unit price
        return frozenUnitPrice.effectivePrice().multiply(BigDecimal.valueOf(quantity));
    }
}