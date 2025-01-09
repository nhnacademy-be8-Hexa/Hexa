package com.nhnacademy.hello.dto.toss;

// 토스 서비스에서 쓰는 정보 객체
public record TossPayment(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Integer totalAmount
) {
}
