package com.nhnacademy.hello.dto.category;

import jakarta.validation.constraints.NotBlank;

public record FirstCategoryRequestDTO(
        @NotBlank
        String categoryName
) {
}

