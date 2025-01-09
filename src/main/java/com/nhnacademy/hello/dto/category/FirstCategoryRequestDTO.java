package com.nhnacademy.hello.dto.category;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record FirstCategoryRequestDTO(
        @Max(20)
        @NotBlank
        String categoryName
) {
}

