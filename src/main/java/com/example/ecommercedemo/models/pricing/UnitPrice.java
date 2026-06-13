package com.example.ecommercedemo.models.pricing;

import com.example.ecommercedemo.models.pricing.snapshots.SnapshotUnitPrice;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UnitPrice {

    @NotNull
    private BasePrice basePrice;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Digits(integer = 3, fraction = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;


    public BigDecimal effectivePrice() {
        BigDecimal original = basePrice.getPrice();
        BigDecimal discount = discountPercentage == null ? BigDecimal.ZERO : discountPercentage;
        BigDecimal discountAmount = original
                .multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return original.subtract(discountAmount);
    }

    public static UnitPrice from(SnapshotUnitPrice snapshot) {
        return UnitPrice.builder()
                .basePrice(snapshot.getSnapshotBasePrice().getBasePrice())
                .discountPercentage(snapshot.getDiscountPercentage())
                .build();
    }



}