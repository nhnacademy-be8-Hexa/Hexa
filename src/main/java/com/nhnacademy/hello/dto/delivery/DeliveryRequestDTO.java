package com.nhnacademy.hello.dto.delivery;

import java.time.LocalDateTime;

public record DeliveryRequestDTO(
        Long orderId,
        Integer deliveryAmount,
        LocalDateTime deliveryDate,
        LocalDateTime deliveryReleaseDate
) {
}
