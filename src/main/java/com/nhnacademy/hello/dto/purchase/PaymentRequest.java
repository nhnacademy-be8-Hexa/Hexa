package com.nhnacademy.hello.dto.purchase;

public record PaymentRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
