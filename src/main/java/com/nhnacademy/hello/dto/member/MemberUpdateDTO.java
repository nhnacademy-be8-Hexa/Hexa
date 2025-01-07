package com.nhnacademy.hello.dto.member;

import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record MemberUpdateDTO(
        @Length(min = 8, max = 20, message = "패스워드는 8~20자이어야 합니다.")
        String memberPassword,
        @Length(min = 2, max = 20, message = "이름은 2~20자이어야 합니다.")
        String memberName,
        @Length(min = 10, max = 11, message = "전화번호는 10~11자이어야 합니다.")
        String memberNumber,
        @Length(max = 320, message = "이메일은 320자 이하이어야 합니다.")
        String memberEmail,
        LocalDate memberBirthAt,
        String ratingId,
        String statusId
) {
}
