package com.example.ecommercedemo.services.pricing;

import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Service
public class PriceService {

    @Value("${spring.frozen_prices_timeout_hours:1}")
    private long timeoutHours;

    private Duration snapshotLifetime;

    @PostConstruct
    public void init() {
        this.snapshotLifetime = Duration.ofHours(timeoutHours);
    }

    // ── Staleness ────────────────────────────────────────────────────

    private boolean isExpired(PriceSnapshot snapshot) {
        if (snapshot == null || snapshot.getTimestamp() == null) {
            return true;
        }
        return Instant.now().isAfter(snapshot.getTimestamp().plus(snapshotLifetime));
    }

    public boolean isSnapshotStale(PriceSnapshot snapshot) {
        return snapshot == null || isExpired(snapshot);
    }

    // ── Snapshot refresh ─────────────────────────────────────────────

    /**
     * If the snapshot is stale (null or expired), fetches a fresh Price from the
     * supplier and builds a new PriceSnapshot from it.
     */
    public PriceSnapshot refreshSnapshot(PriceSnapshot snapshot, Supplier<Price> currentLiveSupplier) {
        if (isSnapshotStale(snapshot)) {
            Price current = currentLiveSupplier.get(); // lazy — only called if stale
            return snapshotFrom(current);
        }
        return snapshot;
    }

    /** Build a PriceSnapshot from a live Price, freezing the computed grossAmount. */
    public PriceSnapshot snapshotFrom(Price live) {
        live.setGrossAmount(computeGrossAmount(live));
        return PriceSnapshot.builder()
                .grossPrice(live.getGrossPrice())
                .grossAmount(live.getGrossAmount())
                .grossDiscount(live.getGrossDiscount())
                .percentageDiscount(live.getPercentageDiscount())
                .quantity(live.getQuantity())
                .timestamp(Instant.now())
                .build();
    }

    // ── Gross amount ─────────────────────────────────────────────────

    /** Compute grossAmount = grossPrice × quantity */
    public static BigDecimal computeGrossAmount(Price price) {
        if (price == null) return BigDecimal.ZERO;
        BigDecimal gp = price.getGrossPrice() != null ? price.getGrossPrice() : BigDecimal.ZERO;
        BigDecimal qty = price.getQuantity() != null ? price.getQuantity() : BigDecimal.ZERO;
        return gp.multiply(qty);
    }

    // ── Effective prices ─────────────────────────────────────────────

    /**
     * Net unit price after percentage discount:
     * grossPrice × (100 − percentageDiscount) / 100
     */
    public static BigDecimal effectiveUnitPrice(Price price) {
        if (price == null) return BigDecimal.ZERO;
        BigDecimal gp = price.getGrossPrice() != null ? price.getGrossPrice() : BigDecimal.ZERO;
        BigDecimal pct = price.getPercentageDiscount() != null ? price.getPercentageDiscount() : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.valueOf(100)
                .subtract(pct)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return gp.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Net line total after all discounts:
     * (grossAmount − grossDiscount) × (100 − percentageDiscount) / 100
     */
    public static BigDecimal effectivePrice(Price price) {
        if (price == null) return BigDecimal.ZERO;
        BigDecimal gross = computeGrossAmount(price);
        BigDecimal discount = price.getGrossDiscount() != null ? price.getGrossDiscount() : BigDecimal.ZERO;
        BigDecimal pct = price.getPercentageDiscount() != null ? price.getPercentageDiscount() : BigDecimal.ZERO;
        BigDecimal afterFlat = gross.subtract(discount);
        BigDecimal multiplier = BigDecimal.valueOf(100)
                .subtract(pct)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return afterFlat.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
