package com.nhnacademy.hello.dto.delivery;

import java.time.LocalDateTime;

public record DeliveryCostPolicyDTO(
        Long deliveryCostPolicyId,
        int deliveryCost,
        int freeMinimumAmount,
        String createdBy,
        LocalDateTime createdAt
) {
}
