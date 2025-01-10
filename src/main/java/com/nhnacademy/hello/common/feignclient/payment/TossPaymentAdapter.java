package com.nhnacademy.hello.common.feignclient.payment;

import com.nhnacademy.hello.dto.toss.TossPaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "hexa-gateway",
        contextId = "tossPaymentAdapter",
        path = "/api/toss-payment"
)
public interface TossPaymentAdapter {

    @PostMapping
    ResponseEntity<?> addPayment(
            @RequestBody TossPaymentDto tossPayment
    );

    @GetMapping("/{orderId}")
    ResponseEntity<TossPaymentDto> getPayment(
            @PathVariable Long orderId
    );
    
}
