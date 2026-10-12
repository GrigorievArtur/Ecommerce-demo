package com.example.ecommercedemo.payments;

public record PaymentResult(boolean success, String transactionId, String message) {

    public static PaymentResult approved(String transactionId) {
        return new PaymentResult(true, transactionId, "Payment approved");
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(false, null, message);
    }
}
