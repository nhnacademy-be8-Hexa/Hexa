package com.nhnacademy.hello.dto.toss;

public record TossPayment(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Integer totalAmount
) {
}
