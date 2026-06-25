package com.example.ecommercedemo.models.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot extends Price {

    /** Used for staleness validation */
    @Builder.Default
    private Instant timestamp = Instant.now();

}
