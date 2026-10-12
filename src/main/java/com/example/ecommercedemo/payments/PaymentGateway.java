package com.example.ecommercedemo.payments;

/**
 * Payment provider abstraction. Swap {@link MockPaymentGateway} for a real provider
 * (Stripe, Adyen, ...) by supplying another {@code @Component} and pointing
 * {@code app.payment.provider} at it.
 */
public interface PaymentGateway {

    PaymentResult charge(PaymentRequest request);

    String providerName();
}
