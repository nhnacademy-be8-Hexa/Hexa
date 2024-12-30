package com.nhnacademy.hello.dto.order;

public record GuestOrderDTO(
        Long orderId,
        String guestOrderNumber,
        String guestOrderEmail
) {
}
