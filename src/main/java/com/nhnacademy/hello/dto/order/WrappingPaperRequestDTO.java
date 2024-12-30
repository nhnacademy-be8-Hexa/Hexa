package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotNull;

public record WrappingPaperRequestDTO(
        @NotNull String wrappingPaperName,
        @NotNull Integer wrappingPaperPrice
) {
}
