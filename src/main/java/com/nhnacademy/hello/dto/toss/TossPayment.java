package com.nhnacademy.hello.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 토스 서비스에서 쓰는 정보 객체
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPayment(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Integer totalAmount
) {
}
