package com.example.ecommercedemo.services.pricing;

import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotBasePrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotLinePrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotUnitPrice;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PriceService {

    private static final Duration SNAPSHOT_LIFETIME = Duration.ofHours(1);



    public boolean isSnapshotStale(SnapshotBasePrice snapshot) {
        return snapshot == null || snapshot.isExpired(SNAPSHOT_LIFETIME);
    }

    public boolean isSnapshotStale(SnapshotUnitPrice snapshot) {
        return snapshot == null || snapshot.isExpired(SNAPSHOT_LIFETIME);
    }

    public boolean isSnapshotStale(SnapshotLinePrice snapshot) {
        return snapshot == null || snapshot.isExpired(SNAPSHOT_LIFETIME);
    }

    // Returns refreshed snapshot if expired, otherwise returns the same one untouched
    public SnapshotBasePrice refreshSnapshot(SnapshotBasePrice snapshot, BasePrice current) {
        if (isSnapshotStale(snapshot)) {
            return SnapshotBasePrice.builder()
                    .basePrice(current)
                    .build();
        }
        return snapshot;
    }

    public SnapshotUnitPrice refreshSnapshot(SnapshotUnitPrice snapshot, UnitPrice current) {
        if (isSnapshotStale(snapshot)) {
            return SnapshotUnitPrice.from(current);
        }
        return snapshot;
    }

    public SnapshotLinePrice refreshSnapshot(SnapshotLinePrice snapshot, LinePrice current) {
        if (isSnapshotStale(snapshot)) {
            return SnapshotLinePrice.from(current);
        }
        return snapshot;
    }
}