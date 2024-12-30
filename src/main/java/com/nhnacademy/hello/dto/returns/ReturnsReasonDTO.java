package com.nhnacademy.hello.dto.returns;

import org.hibernate.validator.constraints.Length;

public record ReturnsReasonDTO(
        Long returnsReasonId,

        @Length(max = 20)
        String returnsReason
) {
}
