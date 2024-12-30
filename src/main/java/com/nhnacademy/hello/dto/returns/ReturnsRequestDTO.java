package com.nhnacademy.hello.dto.returns;

import com.nhnacademy.hello.dto.delivery.DeliveryDTO;

public record ReturnsRequestDTO(
        Long OrderId,
        Long returnsReasonId,
        String returnsDetail

) {

}
