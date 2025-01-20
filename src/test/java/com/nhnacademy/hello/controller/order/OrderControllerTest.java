package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.DeliveryAdapter;
import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.ReturnsAdapter;
import com.nhnacademy.hello.common.feignclient.ReturnsReasonAdapter;
import com.nhnacademy.hello.common.feignclient.payment.TossPaymentAdapter;
import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import com.nhnacademy.hello.dto.toss.TossPaymentDto;
import com.nhnacademy.hello.service.TossService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderAdapter orderAdapter;

    @MockitoBean
    private TossPaymentAdapter tossPaymentAdapter;

    @MockitoBean
    private ReturnsReasonAdapter returnsReasonAdapter;

    @MockitoBean
    private ReturnsAdapter returnsAdapter;

    @MockitoBean
    private DeliveryAdapter deliveryAdapter;

    @MockitoBean
    private TossService tossService;

    @Test
    @DisplayName("주문 취소")
    void testCancelOrder() throws Exception {
        Long orderId = 1L;
        TossPaymentDto paymentDto = mock(TossPaymentDto.class);
        when(tossPaymentAdapter.getPayment(orderId)).thenReturn(ResponseEntity.ok(paymentDto));
        when(tossService.cancelPayment(any(), any(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk());

        verify(tossPaymentAdapter, times(1)).getPayment(orderId);
        verify(tossService, times(1)).cancelPayment(any(), any(), anyInt());
    }

    @Test
    @DisplayName("주문 확정")
    void testConfirmOrder() throws Exception {
        Long orderId = 1L;

        mockMvc.perform(post("/orders/{orderId}/confirm", orderId))
                .andExpect(status().isOk());

        verify(orderAdapter, times(1)).updateOrder(eq(orderId), any());
    }

    @Test
    @DisplayName("반품 신청")
    void testReturnRequestOrder() throws Exception {
        Long orderId = 1L;
        OrderController.ReturnsRequest returnsRequest = new OrderController.ReturnsRequest(1L, "불량품");
        when(returnsReasonAdapter.getReturnsReasonById(returnsRequest.reasonId())).thenReturn(mock(ReturnsReasonDTO.class));

        mockMvc.perform(post("/orders/{orderId}/return-request", orderId)
                        .contentType("application/json")
                        .content("{\"reasonId\":1, \"returnsDetail\":\"불량품\"}"))
                .andExpect(status().isOk());

        verify(orderAdapter, times(1)).updateOrder(eq(orderId), any());
        verify(returnsAdapter, times(1)).createReturns(any());
    }

    @Test
    @DisplayName("반품 처리")
    void testReturnOrder() throws Exception {
        Long orderId = 1L;
        TossPaymentDto paymentDto = new TossPaymentDto(1L, "key", 10000);
        when(tossPaymentAdapter.getPayment(orderId)).thenReturn(ResponseEntity.ok(paymentDto));
        DeliveryDTO deliveryDTO = new DeliveryDTO(1L, 3000, LocalDateTime.now(), LocalDateTime.now(), null);
        when(deliveryAdapter.getDelivery(orderId)).thenReturn(deliveryDTO);
        when(tossService.cancelPayment(any(), any(), anyInt())).thenReturn(ResponseEntity.ok().build());

        ReturnsDTO returnsDTO = new ReturnsDTO(1L, new ReturnsReasonDTO(1L, "a"), "a", null);
        when(returnsAdapter.getReturnsByOrderId(anyLong())).thenReturn(returnsDTO);

        mockMvc.perform(post("/orders/{orderId}/return", orderId))
                .andExpect(status().isOk());

        verify(tossService, times(1)).cancelPayment(any(), any(), anyInt());
        verify(orderAdapter, times(1)).updateOrder(eq(orderId), any());
    }

}