package com.nhnacademy.hello.dto.order;

import com.nhnacademy.hello.dto.book.BookDTO;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long orderId,
        Integer orderPrice,
        LocalDateTime orderedAt,
        WrappingPaperDTO wrappingPaper,
        OrderStatusDTO orderStatus,
        String zoneCode,
        String address,
        String addressDetail,
        MemberDTO member,
        List<BookDTO> books // 주문에 포함된 도서 목록
) {
    public record MemberDTO(String memberId,String memberName, String memberNumber) {}
}