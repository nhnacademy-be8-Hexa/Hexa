package com.nhnacademy.hello.dto.member;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberRequestDTO(
        @Length(min = 3, max = 20)
        @NotNull
        String memberId,
        @Length(min = 8, max = 60)
        @NotNull
        String memberPassword,
        @Length(min = 2, max = 20)
        @NotNull
        String memberName,
        @Length(min = 10, max = 11)
        @NotNull
        String memberNumber,
        @Length(max = 320)
        String memberEmail,
        LocalDate memberBirthAt,
        LocalDateTime memberLastLoginAt,
        @NotNull
        String ratingId,
        @NotNull
        String statusId
) {
}
