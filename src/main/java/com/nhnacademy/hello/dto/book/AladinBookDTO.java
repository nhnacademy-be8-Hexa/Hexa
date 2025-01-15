package com.nhnacademy.hello.dto.book;

import java.util.List;

public record AladinBookDTO(
        String title,
        List<String> authors,
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
