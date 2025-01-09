package com.nhnacademy.hello.dto.toss;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;// 진짜 엔티티 용 객체

public record TossPaymentDto(
        @Length(max = 50)
        String paymentKey,

        @NotNull
        @Length(max = 70)
        String orderId,

        @NotNull
        int amount
) {
}
