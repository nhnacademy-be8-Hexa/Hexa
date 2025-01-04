package com.nhnacademy.hello.dto.category;

import jakarta.validation.constraints.NotNull;

public record SecondCategoryRequestDTO(
        @NotNull
        Long categoryId,
        @NotNull
        Long subCategoryId
) {
}