package com.example.ecommercedemo.models.pricing;

import jakarta.persistence.Embeddable;
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
public class LinePrice {

    int quantity;
    UnitPrice unitPrice;

    /**
     * Computed — never stored. Delegates to UnitPrice.effectivePrice().
     */
    public BigDecimal totalPrice() {
        return unitPrice.effectivePrice()
                .multiply(BigDecimal.valueOf(quantity));
    }

}
