package com.nhnacademy.hello.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookUpdateRequestDTO(
        @Size(max = 100)
        String bookTitle,

        @Size(min = 10, max = 10000)
        String bookDescription,

        @Positive
        Integer bookPrice,

        Boolean bookWrappable,

        String statusId
) {
}
