package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.ReturnsAdapter;
import com.nhnacademy.hello.common.feignclient.ReturnsReasonAdapter;
import com.nhnacademy.hello.common.feignclient.payment.TossPaymentAdapter;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import com.nhnacademy.hello.dto.returns.ReturnsRequestDTO;
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

    private final ReturnsReasonAdapter returnsReasonAdapter;
    private final ReturnsAdapter returnsAdapter;

    private final TossService tossService;


    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderId") Long orderId
    ) {

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

        // 주문 상태 변경 -> CANCELED 5
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

    // 사용자의 반품 신청
    @PostMapping("/orders/{orderId}/return-request")
    public ResponseEntity<?> returnRequestOrder(
            @PathVariable("orderId") Long orderId,
            @RequestBody ReturnsRequest returnsRequest
            ){

        // 반품 사유 조회
        ReturnsReasonDTO returnsReasonDTO = returnsReasonAdapter.getReturnsReasonById(returnsRequest.reasonId());

        // 주문 상태 변경 -> RETURN_REQUEST 6
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                null,
                null,
                null,
                6L,
                null,
                null,
                null
        );
        orderAdapter.updateOrder(orderId, orderRequestDTO);

        // 반품 정보 저장
        ReturnsRequestDTO returnsRequestDTO = new ReturnsRequestDTO(
                orderId,
                returnsRequest.reasonId(),
                returnsRequest.returnsDetail()
        );
        returnsAdapter.createReturns(returnsRequestDTO);

        return ResponseEntity.ok().build();
    }

    public record ReturnsRequest(
            Long reasonId,
            String returnsDetail
    ){
    }

    // 관리자의 반품 처리
    @PostMapping("/orders/{orderId}/return")
    public ResponseEntity<?> returnOrder(
            @PathVariable("orderId") Long orderId
    ){

        // 토스 결제 정보 조회
        TossPaymentDto payment = tossPaymentAdapter.getPayment(orderId).getBody();

        // 환불 비용이 3000원 이상일 경우에만 환불 해줌
        if(payment.amount() > 3000) {

            // 반품 사유 조회
            ReturnsDTO returnsDTO = returnsAdapter.getReturnsByOrderId(orderId);
            ReturnsReasonDTO returnsReasonDTO = returnsReasonAdapter.getReturnsReasonById(returnsDTO.returnsReasonId());

            // 토스 주문 취소 처리
            ResponseEntity<?> response = tossService.cancelPayment(
                    payment.paymentKey(),
                    "구매자 반품 신청: " + returnsReasonDTO.returnsReason(),
                    payment.amount() - 3000 // 배송비 제외한 금액 환불
            );

            if(response.getStatusCode() != HttpStatus.OK) {
                // 토스 처리 오류
                return response;
            }

        }

        // 주문 상태 변경 -> RETURNED 4
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                null,
                null,
                null,
                4L,
                null,
                null,
                null
        );
        orderAdapter.updateOrder(orderId, orderRequestDTO);

        return ResponseEntity.ok().build();
    }

}
