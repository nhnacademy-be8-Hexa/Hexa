package com.nhnacademy.hello.dto.review;

import java.math.BigDecimal;

public record ReviewDTO(
        Long reviewId,
        String reviewContent,
        BigDecimal reviewRating,
        MemberDTO member,
        Boolean reviewIsBlocked
) {
    public record MemberDTO(String memberId) {}
}

