package com.nhnacademy.hello.dto.delivery;

import jakarta.validation.constraints.NotNull;

public record DeliveryCostPolicyRequest(
        @NotNull int deliveryCost,
        @NotNull int freeMinimumAmount,
        @NotNull String createdBy
) {
}
