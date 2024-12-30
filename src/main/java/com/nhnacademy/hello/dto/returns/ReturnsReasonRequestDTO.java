package com.nhnacademy.hello.dto.returns;

import org.hibernate.validator.constraints.Length;

public record ReturnsReasonRequestDTO(
        @Length(max = 20) String returnsReason) {
}
