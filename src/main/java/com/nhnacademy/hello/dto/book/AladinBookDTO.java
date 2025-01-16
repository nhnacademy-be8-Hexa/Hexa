package com.nhnacademy.hello.dto.book;

import java.time.LocalDate;
import java.util.List;

public record AladinBookDTO(
        String title,
        List<String> authors,
        int priceSales,
        int priceStandard,
        String publisher,
        LocalDate pubDate,
        Long isbn13,
        String description,
        int salesPoint,
        String cover
) {
}
