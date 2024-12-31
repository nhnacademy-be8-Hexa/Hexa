package com.nhnacademy.hello.dto.returns;

import org.hibernate.validator.constraints.Length;

public record ReturnsRequestDTO(
        Long OrderId,
        Long returnsReasonId,
        @Length(max = 100)
        String returnsDetail
) {

}
