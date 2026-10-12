package com.example.ecommercedemo.payments;

import java.math.BigDecimal;

/**
 * Provider-agnostic payment request.
 *
 * <p><strong>Security note:</strong> in the mock/demo flow raw card details are held in
 * memory only for the duration of the call and are never persisted. A production
 * integration should send a provider token (e.g. a Stripe PaymentMethod id) instead of a
 * PAN — see {@code README-HANDOFF.md}.</p>
 */
public record PaymentRequest(
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        String cardNumber,
        String cardHolder,
        String expiry,
        String cvc
) {
}
