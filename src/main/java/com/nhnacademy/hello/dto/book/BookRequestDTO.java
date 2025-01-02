package com.nhnacademy.hello.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record BookRequestDTO(
        @NotBlank
        @Size(max = 100)
        String bookTitle,

        @NotBlank
        @Size(min = 10, max = 10000)
        String bookDescription,

        LocalDate bookPubDate,

        @NotNull
        @Digits(integer = 13, fraction = 0)
        Long bookIsbn,

        @NotNull
        @Positive
        Integer bookOriginPrice,

        @NotNull
        @Positive
        Integer bookPrice,

        @NotNull
        boolean bookWrappable,

        @NotNull
        String publisherId,

        @NotNull
        String bookStatusId,

        List<Long> categoryIds


) {
        // 기본 생성자 제공
        public BookRequestDTO() {
                this(
                        "",
                        "",
                        null,
                        null,
                        null,
                        null,
                        false,
                        "",
                        "",
                        new ArrayList<>()
                );
        }

}
