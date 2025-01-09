package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderAdapter orderAdapter;


    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderId") Long orderId
    ) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();


        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/return")
    public ResponseEntity<?> returnOrder(
            @PathVariable("orderId") Long orderId,
            @RequestBody ReturnsRequest returnsRequest
            ){


        return ResponseEntity.ok().build();
    }

    public record ReturnsRequest(
            Long reasonId,
            String returnsDetail
    ){
    }

}
