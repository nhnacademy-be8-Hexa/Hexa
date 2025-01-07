package com.nhnacademy.hello.dto.returns;

public record ReturnsDTO(
        Long orderId,
        Long returnsReasonId,
        String returnsDetail,
        OrderDTO order
) {
    public record OrderDTO(Long orderID) {}
}
