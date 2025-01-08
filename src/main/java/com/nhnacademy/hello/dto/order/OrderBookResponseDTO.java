package com.nhnacademy.hello.dto.order;

public record OrderBookResponseDTO(
        Long orderBookId,    // 주문서 ID
        Long bookId,         // 책 ID
        String bookTitle,    // 책 제목
        Integer orderBookAmount,   // 주문 수량
        Integer bookPrice,         // 책 가격
        Long couponId             // 쿠폰 ID (있을 경우)
) {
}
