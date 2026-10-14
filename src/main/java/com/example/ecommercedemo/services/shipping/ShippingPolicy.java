package com.example.ecommercedemo.services.shipping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Flat-rate delivery with a free-delivery threshold, shared by the cart, checkout and
 * order confirmation so the figure a customer sees never changes between steps.
 */
@Service
public class ShippingPolicy {

    @Value("${app.shipping.free-threshold:75}")
    private BigDecimal freeThreshold;

    @Value("${app.shipping.flat-rate:5.99}")
    private BigDecimal flatRate;

    public BigDecimal freeThreshold() {
        return freeThreshold;
    }

    public BigDecimal flatRate() {
        return flatRate;
    }

    /** Delivery fee for a given item subtotal. Empty carts are never charged. */
    public BigDecimal feeFor(BigDecimal subtotal) {
        BigDecimal amount = subtotal == null ? BigDecimal.ZERO : subtotal;
        if (amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.compareTo(freeThreshold) >= 0 ? BigDecimal.ZERO : flatRate;
    }

    /** How much more the customer must spend before delivery becomes free. */
    public BigDecimal amountUntilFreeDelivery(BigDecimal subtotal) {
        BigDecimal amount = subtotal == null ? BigDecimal.ZERO : subtotal;
        BigDecimal remaining = freeThreshold.subtract(amount);
        return remaining.signum() > 0 ? remaining.setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
