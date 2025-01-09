package com.nhnacademy.hello.dto.toss;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

// 진짜 엔티티 용 객체
public record TossPaymentDto(
        @NotNull
        Long orderId,

        @NotNull
        @Length(max = 50)
        String paymentKey,

        @NotNull
        int amount
) {
}
