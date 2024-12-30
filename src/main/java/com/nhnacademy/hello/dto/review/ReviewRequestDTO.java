package com.nhnacademy.hello.dto.review;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ReviewRequestDTO(
        @NotNull
        @Length(max = 400)
        String reviewContent,

        @NotNull
        BigDecimal reviewRating
) {
}
