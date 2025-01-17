package com.nhnacademy.hello.dto.book;

import java.time.LocalDate;

public record BookDTO(
        Long bookId,
        String bookTitle,
        String bookDescription,
        LocalDate bookPubDate,
        Long bookIsbn,
        int bookOriginPrice,
        int bookPrice,
        Boolean bookWrappable,
        int bookView,
        int bookAmount,
        Long bookSellCount,
        PublisherRequestDTO publisher,
        BookStatusRequestDTO bookStatus,
        String imagePath
) {
}
