package com.nhnacademy.hello.dto.book;

public record SearchBookDTO(
        Long bookId,
        String bookTitle,
        String bookDescription,
        String bookPubDate,
        Long bookIsbn,
        int bookOriginPrice,
        int bookPrice,
        boolean bookWrappable,
        int bookView,
        int bookAmount,
        Long bookSellCount,
        PublisherRequestDTO publisher,
        BookStatusRequestDTO bookStatus,
        String imagePath
) {

}