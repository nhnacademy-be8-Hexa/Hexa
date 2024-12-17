package com.nhnacademy.hello.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberRegisterDTO(
        @Length(min = 3, max = 20)
        @NotNull
        String memberId,

        // 이거는 암호화되지 않은 암호이므로 20자 제한
        @Length(min = 8, max = 20)
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
        LocalDate memberBirthAt
) {
}
