package com.nhnacademy.hello.dto.member;

public record RatingDTO(
        Long ratingId,
        String ratingName,
        Integer ratingPercent
) {
}
