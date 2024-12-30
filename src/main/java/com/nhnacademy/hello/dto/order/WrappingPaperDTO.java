package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record WrappingPaperDTO(
        Long wrappingPaperId,

        @NotBlank
        @Length(max = 20)
        String wrappingPaperName,

        Integer wrappingPaperPrice
) {
}
