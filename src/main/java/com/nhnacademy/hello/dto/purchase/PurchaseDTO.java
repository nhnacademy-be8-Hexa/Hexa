package com.nhnacademy.hello.dto.purchase;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseDTO(
        String paymentKey,
        String orderId,
        int amount,
        List<PurchaseBookDTO> books,
        String zoneCode,
        String address,
        String addressDetail,
        Long wrappingPaperId,
        Integer usingPoint,
        LocalDateTime deliveryDate,
        String guestOrderNumber,
        String guestEmail,
        String guestPassword,
        List<Long> selectedCouponIds,
        int deliveryAmount
) {
}

