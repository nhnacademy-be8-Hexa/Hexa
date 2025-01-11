package com.nhnacademy.hello.dto.order;

import java.time.LocalDateTime;

public record OrderDTO(
        Long orderId,
        Integer orderPrice,
        LocalDateTime orderedAt,
        WrappingPaperDTO wrappingPaper,
        OrderStatusDTO orderStatus,
        String zoneCode,
        String address,
        String addressDetail,
        MemberDTO member
) {
    public record MemberDTO(String memberId,String memberName, String memberNumber, String memberEmail) {}
}