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
        // 도서 제목
        @NotBlank
        @Size(max = 100)
        String bookTitle,

        // 도서 설명
        @NotBlank
        @Size(min = 10, max = 10000)
        String bookDescription,

        // 출판일자
        LocalDate bookPubDate,

        // 도서 ISBN
        @NotNull
        @Digits(integer = 13, fraction = 0)
        Long bookIsbn,

        // 도서 원가
        @NotNull
        @Positive
        Integer bookOriginPrice,

        // 도서 판매가
        @NotNull
        @Positive
        Integer bookPrice,

        // 포장 여부
        @NotNull
        boolean bookWrappable,

        // 출판사 아이디
        @NotNull
        String publisherId,

        // 도서 상태 아이디
        @NotNull
        String bookStatusId,

        // 카테고리 아아디 리스트
        List<Long> categoryIds,

        // 도서 수량
        Integer bookAmount
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
                        new ArrayList<>(),
                        1
                );
        }

}
