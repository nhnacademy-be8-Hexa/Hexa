package com.nhnacademy.hello.dto.purchase;

import java.util.List;

public record PurchaseDTO(
        String paymentKey,
        String orderId,
        int amount,
        List<PurchaseBookDTO> books,
        String zoneCode,
        String address,
        String addressDetail,
        Long wrappingPaperId
) {
}

