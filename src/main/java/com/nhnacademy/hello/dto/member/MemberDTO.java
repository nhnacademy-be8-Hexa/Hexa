package com.nhnacademy.hello.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberDTO(
        String memberId,
        String memberPassword,
        String memberName,
        String memberNumber,
        LocalDate memberBirthAt,
        LocalDate memberCreatedAt,
        LocalDateTime memberLastLoginAt,
        String memberRole,
        RatingDTO rating,
        MemberStatusDTO memberStatus
) {
}
