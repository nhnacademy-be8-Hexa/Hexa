package com.nhnacademy.hello.dto.returns;

public record ReturnsDTO(
        Long OrderId,
        Long returnsReasonId,
        String returnsDetail,
        OrderDTO order
) {
    public record OrderDTO(Long OrderID) {}
}
