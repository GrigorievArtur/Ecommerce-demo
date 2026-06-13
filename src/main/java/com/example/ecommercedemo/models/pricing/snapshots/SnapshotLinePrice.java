package com.example.ecommercedemo.models.pricing.snapshots;

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
import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SnapshotLinePrice {

    @Min(1)
    private int quantity;

    @NotNull
    @Embedded
    private SnapshotUnitPrice snapshotUnitPrice;

    @NotNull
    private BigDecimal snapshotEffectivePrice;

    public static SnapshotLinePrice from(LinePrice live) {
        return SnapshotLinePrice.builder()
                .quantity(live.getQuantity())
                .snapshotUnitPrice(SnapshotUnitPrice.from(live.getUnitPrice()))
                .snapshotEffectivePrice(live.totalPrice())
                .build();
    }

    public boolean isExpired(Duration ttl) {
        return snapshotUnitPrice.isExpired(ttl);
    }

    public BigDecimal effectivePrice() {
        return snapshotEffectivePrice;
    }

    public BigDecimal totalPrice() {
        return snapshotUnitPrice.effectivePrice()
                .multiply(BigDecimal.valueOf(quantity));
    }

}
