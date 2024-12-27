package com.nhnacademy.hello.dto.cart;

public record CartRequestDTO(
        String memberId,
        Long bookId,
        Integer cartAmount
) {
}
