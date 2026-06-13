package com.example.ecommercedemo.models.pricing.frozen;

import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FrozenPrice {

    @NotNull
    private BigDecimal frozenEffectivePrice;

    @NotNull
    @Builder.Default
    private Instant snapshotTime = Instant.now();

    public static FrozenPrice freeze(BasePrice live) {
        return FrozenPrice.builder()
                .frozenEffectivePrice(live.effectivePrice())
                .build();
    }

    public static FrozenPrice freeze(UnitPrice live) {
        return FrozenPrice.builder()
                .frozenEffectivePrice(live.effectivePrice())
                .build();
    }

    public static FrozenPrice freeze(LinePrice live) {
        return FrozenPrice.builder()
                .frozenEffectivePrice(live.effectivePrice())
                .build();
    }

    public BigDecimal effectivePrice() {
        return this.frozenEffectivePrice;
    }
}