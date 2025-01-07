package com.nhnacademy.hello.dto.tag;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;

public record TagRequestDTO(
        @NotBlank @Length(max = 30) String tagName
) {
    // 기본 생성자 제공
    public TagRequestDTO() {
        this(
                ""
        );
    }
}
