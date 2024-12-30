package com.nhnacademy.hello.dto.returns;

import org.hibernate.validator.constraints.Length;

public record ReturnsDTO(
        Long OrderId,
        Long returnsReasonId,
        @Length(max = 100)
        String returnsDetail,
        OrderDTO order
) {
    public record OrderDTO(Long OrderID) {}
}
