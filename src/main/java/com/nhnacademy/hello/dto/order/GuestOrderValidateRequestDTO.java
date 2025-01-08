package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record GuestOrderValidateRequestDTO(
        @NotNull
        Long orderId,

        @NotNull
        @Length(max = 60)
        String guestOrderPassword
){
}
