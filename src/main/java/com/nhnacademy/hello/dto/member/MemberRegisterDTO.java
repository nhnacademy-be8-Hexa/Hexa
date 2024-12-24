package com.nhnacademy.hello.dto.member;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record MemberRegisterDTO(
        @Length(min = 3, max = 20, message = "아이디는 3~20 글자이어야 합니다.")
        @NotNull
        String memberId,

        // 이거는 암호화되지 않은 암호이므로 20자 제한
        @Length(min = 8, max = 20, message = "패스워드는 8~20 글자이어야 합니다.")
        @NotNull
        String memberPassword,

        @Length(min = 2, max = 20, message = "이름은 2~20글자이어야 합니다.")
        @NotNull
        String memberName,
        @Length(min = 10, max = 11, message = "전화번호는 10~11자이어야 합니다.")
        @NotNull
        String memberNumber,
        @Length(max = 320, message = "이메일은 최대 320자이어야 합니다.")
        String memberEmail,
        LocalDate memberBirthAt
) {
}
