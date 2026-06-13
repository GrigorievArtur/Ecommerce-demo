package com.example.ecommercedemo.models.pricing;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LinePrice {

    @Min(1)
    @Builder.Default
    private int quantity = 1;

    @NotNull
    @Embedded
    private UnitPrice unitPrice;

    public BigDecimal effectivePrice() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.effectivePrice().multiply(BigDecimal.valueOf(quantity));
    }
}