package com.nhnacademy.hello.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FirstCategoryRequestDTO(

        @NotBlank
        @Size(max = 20, message = "카테고리 이름은 20자 이하로 입력해야 합니다.")
        String categoryName
) {
}

