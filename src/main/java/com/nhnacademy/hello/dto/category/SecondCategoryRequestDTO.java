package com.nhnacademy.hello.dto.category;

import jakarta.validation.constraints.NotNull;

public record SecondCategoryRequestDTO(
        Long categoryId,
        @NotNull
        Long subCategoryId,

        Long parentCategoryId
) {
}
