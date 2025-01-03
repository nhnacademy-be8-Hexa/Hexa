package com.nhnacademy.hello.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberDTO(
        String memberId,
        String memberName,
        String memberNumber,
        String memberEmail,
        LocalDate memberBirthAt,
        LocalDate memberCreatedAt,
        LocalDateTime memberLastLoginAt,
        String memberRole,
        RatingDTO rating,
        MemberStatusDTO memberStatus
) {
}
