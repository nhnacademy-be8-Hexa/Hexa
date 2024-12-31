package com.nhnacademy.hello.dto.order;

public record WrappingPaperDTO(
        Long wrappingPaperId,
        String wrappingPaperName,
        Integer wrappingPaperPrice
) {
}
