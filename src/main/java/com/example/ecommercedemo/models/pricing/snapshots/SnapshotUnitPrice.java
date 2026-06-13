package com.example.ecommercedemo.models.pricing.snapshots;

import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
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
import java.time.Duration;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SnapshotUnitPrice {

//    @NotNull
//    @DecimalMin("0.01")
//    @Digits(integer = 10, fraction = 2)
    private SnapshotBasePrice snapshotBasePrice;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Digits(integer = 3, fraction = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;


    /**
     * The effective price at snapshot time — stored explicitly so it
     * never changes even if rounding logic changes later.
     */

    @NotNull
    private BigDecimal snapshotEffectivePrice;

    public static SnapshotUnitPrice from(UnitPrice live) {
        return SnapshotUnitPrice.builder()
                .snapshotBasePrice(SnapshotBasePrice.builder()
                        .basePrice(live.getBasePrice())
                        .build())
                .discountPercentage(live.getDiscountPercentage())
                .snapshotEffectivePrice(live.effectivePrice())
                .build();
    }

    /**
     * Always returns the price frozen at snapshot time.
     */
    public BigDecimal effectivePrice() {
        return snapshotEffectivePrice;
    }

    public boolean isExpired(Duration ttl) {
        return snapshotBasePrice.isExpired(ttl);
    }
}