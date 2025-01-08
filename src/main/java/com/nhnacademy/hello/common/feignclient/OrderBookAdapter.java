package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hexa-gateway", contextId = "orderBookAdapter")
public interface OrderBookAdapter {

    @GetMapping("api/orders/{orderId}/orderBooks")
    OrderBookResponseDTO [] getOrderBooksByOrderId(@PathVariable long orderId);
}
