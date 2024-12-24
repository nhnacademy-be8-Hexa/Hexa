package com.nhnacademy.hello.dto.member;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record MemberUpdateDTO(
        @Length(min = 8, max = 20)
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
        String ratingId,
        String statusId
) {
}
