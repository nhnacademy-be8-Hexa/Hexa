package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record OrderStatusDTO(
        Long orderStatusId,

        @NotBlank
        @Length(max = 20)
        String orderStatus
) {
}
