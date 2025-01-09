package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.payment.TossPaymentAdapter;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import com.nhnacademy.hello.dto.toss.TossPaymentDto;
import com.nhnacademy.hello.service.TossService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderAdapter orderAdapter;
    private final TossPaymentAdapter tossPaymentAdapter;

    private final TossService tossService;


    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderId") Long orderId
    ) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

        // 결제 정보 조회
        TossPaymentDto payment = tossPaymentAdapter.getPayment(orderId).getBody();

        // 토스 주문 취소 처리
        ResponseEntity<?> response = tossService.cancelPayment(
                payment.paymentKey(),
                "구매자 주문 취소",
                payment.amount()
                );

        if(response.getStatusCode() != HttpStatus.OK) {
            // 토스 처리 오류
            return response;
        }

        // 주문 상태 변경 -> CANCELED
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                null,
                null,
                null,
                5L,
                null,
                null,
                null
        );
        orderAdapter.updateOrder(orderId, orderRequestDTO);


        return response;
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
