package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotNull;

public record OrderStatusRequestDTO(
        @NotNull String orderStatus
) {
}
