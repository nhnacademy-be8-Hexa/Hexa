package com.nhnacademy.hello.dto.book;

public record BookSearchDTO(
        Long bookId,
        String bookTitle,
        String bookDescription,
        String bookPubDate,
        Long bookIsbn,
        int bookOriginPrice,
        int bookPrice,
        Boolean bookWrappable,
        int bookView,
        int bookAmount,
        Long bookSellCount,
        String publisherName,
        String bookStatus,
        String imagePath
) {
}
