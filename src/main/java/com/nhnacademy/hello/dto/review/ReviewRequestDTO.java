package com.nhnacademy.hello.dto.review;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ReviewRequestDTO(
        @NotNull(message = "리뷰 내용을 입력해주세요.")
        @Length(max = 400, message = "리뷰 내용은 최대 400자입니다.")
        String reviewContent,

        @NotNull(message = "평점을 선택해주세요.")
        BigDecimal reviewRating
) {
}
