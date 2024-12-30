package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record GuestOrderDTO(
        Long orderId,

        @NotBlank
        @Length(max = 11)
        String guestOrderNumber,

        @NotBlank
        @Length(max = 320)
        String guestOrderEmail
) {
}
