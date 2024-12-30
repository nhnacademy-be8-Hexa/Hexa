package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record GuestOrderRequestDTO(
        @NotNull
        Long orderId,

        @NotNull
        @Length(max = 60)
        String guestOrderPassword,

        @NotNull
        @Length(max = 11)
        String guestOrderNumber,

        @NotNull
        @Length(max = 320)
        String guestOrderEmail

) {
}
