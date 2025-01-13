package com.nhnacademy.hello.dto.returns;


public record ReturnsDTO(
        Long orderId,
        ReturnsReasonDTO returnsReason, // JSON 응답의 "returnsReason" 객체를 매핑
        String returnsDetail,
        OrderDTO order
) {
    public record OrderDTO(Long orderID) {}
}
