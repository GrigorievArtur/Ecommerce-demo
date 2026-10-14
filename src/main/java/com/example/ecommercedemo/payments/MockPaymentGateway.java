package com.example.ecommercedemo.payments;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Deterministic sandbox gateway used for local development and demos.
 *
 * <ul>
 *     <li>Any card ending in <code>0000</code> is declined.</li>
 *     <li>A missing/very short card number is declined.</li>
 *     <li>PayPal / WebMoney are approved without card details.</li>
 *     <li>Everything else is approved with a fake transaction id.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            return PaymentResult.declined("Invalid amount");
        }
        if (request.method() == PaymentMethod.CARD) {
            String pan = request.cardNumber() == null ? "" : request.cardNumber().replaceAll("\\s", "");
            if (pan.length() < 12) {
                return PaymentResult.declined("Please enter a valid card number");
            }
            if (pan.endsWith("0000")) {
                return PaymentResult.declined("Card declined by issuer");
            }
        }
        return PaymentResult.approved("mock_" + UUID.randomUUID());
    }

    @Override
    public String providerName() {
        return "mock";
    }
}
