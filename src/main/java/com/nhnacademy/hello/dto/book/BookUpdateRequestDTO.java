package com.nhnacademy.hello.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookUpdateRequestDTO(
        @NotBlank
        @Size(max = 100)
        String bookTitle,

        @NotBlank
        @Size(min = 10, max = 10000)
        String bookDescription,

        @NotNull
        @Positive
        int bookPrice,

        @NotNull
        Boolean bookWrappable,

        @NotNull
        String statusId
) {
}
