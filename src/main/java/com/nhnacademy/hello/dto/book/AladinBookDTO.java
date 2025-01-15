package com.nhnacademy.hello.dto.book;

public record AladinBookDTO(
        String title,
        String author,
        String priceSales,
        String priceStandard,
        String publisher,
        String pubDate,
        String isbn13,
        String description,
        String categoryName,
        String salesPoint,
        String cover
) {
}
