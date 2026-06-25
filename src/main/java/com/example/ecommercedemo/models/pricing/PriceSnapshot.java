package com.example.ecommercedemo.models.pricing;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot extends Price {

    // used for time validation
    private Instant timestamp;

}
