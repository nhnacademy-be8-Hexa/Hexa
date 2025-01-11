package com.nhnacademy.hello.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GuestOrderDTO(
        @JsonProperty("orderId") Long orderId,
        @JsonProperty("guestOrderNumber") String guestOrderNumber,
        @JsonProperty("guestOrderEmail") String guestOrderEmail
) {
}
