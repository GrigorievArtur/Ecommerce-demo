package com.example.ecommercedemo.models.pricing.snapshots;

import com.example.ecommercedemo.models.pricing.BasePrice;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SnapshotBasePrice {

    @Embedded
    private BasePrice basePrice;

    @NotNull
    @Builder.Default
    private Instant snapshotTime = Instant.now();

    public boolean isExpired(Duration ttl) {
        return Instant.now().isAfter(snapshotTime.plus(ttl));
    }

}