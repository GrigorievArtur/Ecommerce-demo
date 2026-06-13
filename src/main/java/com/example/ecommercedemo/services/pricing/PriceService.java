package com.example.ecommercedemo.services.pricing;

import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenUnitPrice;
import jakarta.annotation.PostConstruct;
import jakarta.el.LambdaExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Service
public class PriceService {

    @Value("${spring.frozen_prices_timeout_hours:1}")
    private long timeoutHours;

    // Changed: removed static final so it can be safely computed dynamically at runtime
    private Duration snapshotLifetime;

    /**
     * Executes automatically after Spring completes property injection tasks.
     */
    @PostConstruct
    public void init() {
        this.snapshotLifetime = Duration.ofHours(timeoutHours);
    }

    // Core expiration helper utility
    private boolean isExpired(FrozenPrice frozenPrice) {
        if (frozenPrice == null || frozenPrice.getSnapshotTime() == null) {
            return true;
        }
        return Instant.now().isAfter(frozenPrice.getSnapshotTime().plus(snapshotLifetime));
    }

    public boolean isSnapshotStale(FrozenPrice snapshot) {
        return snapshot == null || isExpired(snapshot);
    }

    public boolean isSnapshotStale(FrozenUnitPrice snapshot) {
        return snapshot == null || snapshot.getFrozenPrice() == null || isExpired(snapshot.getFrozenPrice());
    }

    public boolean isSnapshotStale(FrozenLinePrice snapshot) {
        return snapshot == null
                || snapshot.getFrozenUnitPrice() == null
                || snapshot.getFrozenUnitPrice().getFrozenPrice() == null
                || isExpired(snapshot.getFrozenUnitPrice().getFrozenPrice());
    }

    public FrozenPrice refreshSnapshot(FrozenPrice frozenPrice, Supplier<BasePrice> currentLiveSupplier) {
        if (isSnapshotStale(frozenPrice)) {
            BasePrice current = currentLiveSupplier.get();
        }
        return frozenPrice;
    }

    public FrozenUnitPrice refreshSnapshot(FrozenUnitPrice snapshot, Supplier<UnitPrice> currentLiveSupplier) {
        if (isSnapshotStale(snapshot)) {
            UnitPrice current = currentLiveSupplier.get(); // Lazy execution happens here
            return FrozenUnitPrice.builder()
                    .frozenPrice(FrozenPrice.freeze(current))
                    .discountPercentage(current.getDiscountPercentage())
                    .build();
        }
        return snapshot;
    }

    public FrozenLinePrice refreshSnapshot(FrozenLinePrice snapshot, Supplier<LinePrice> currentLiveSupplier) {
        if (isSnapshotStale(snapshot)) {
            LinePrice current = currentLiveSupplier.get(); // Lazy execution happens here
            return FrozenLinePrice.builder()
                    .quantity(current.getQuantity())
                    .frozenUnitPrice(FrozenUnitPrice.builder()
                            .frozenPrice(FrozenPrice.freeze(current.getUnitPrice()))
                            .discountPercentage(current.getUnitPrice().getDiscountPercentage())
                            .build())
                    .build();
        }
        return snapshot;
    }
}