package com.nhnacademy.hello.dto.book;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BookUpdateRequestDTO(
        @Size(max = 100)
        String bookTitle,

        @Size(min = 10, max = 10000)
        String bookDescription,

        @Positive
        Integer bookPrice,

        Boolean bookWrappable,

        @Positive
        Integer bookAmount,

        String statusId,

        List<Long> categoryIds
) {
}
