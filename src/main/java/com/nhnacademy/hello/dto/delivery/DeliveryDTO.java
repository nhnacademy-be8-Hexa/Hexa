package com.nhnacademy.hello.dto.delivery;

import java.time.LocalDateTime;

public record DeliveryDTO(
        Long orderId,
        Integer deliveryAmount,
        LocalDateTime deliveryDate,
        LocalDateTime deliveryReleaseDate,
        OrderDTO order
) {
    public record OrderDTO(Long OrderId) {}
}
