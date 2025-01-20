package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.order.GuestOrderValidateRequestDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestOrderController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class GuestOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderAdapter orderAdapter;

    @MockitoBean
    private OrderBookAdapter orderBookAdapter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private DeliveryAdapter deliveryAdapter;

    @MockitoBean
    private ReturnsReasonAdapter returnsReasonAdapter;

    @Test
    @DisplayName("비회원 주문 조회 로그인 창 표시")
    void guestOrderPage_ShouldReturnGuestOrderLoginView() throws Exception {
        mockMvc.perform(get("/guestOrder"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/guestOrderLogin"));
    }

    @Test
    @DisplayName("비회원 주문 조회 페이지 성공")
    void checkGuestOrder_WithValidData_ShouldReturnGuestOrderInfoView() throws Exception {
        Long orderId = 123L;
        String guestOrderPassword = "password";
        String encodedPassword = "encodedPassword";

        // Mocking
        when(passwordEncoder.encode(guestOrderPassword)).thenReturn(encodedPassword);
        when(orderAdapter.getGuestOrderPassword(any(GuestOrderValidateRequestDTO.class)))
                .thenReturn(ResponseEntity.ok(encodedPassword));
        when(passwordEncoder.matches(eq(guestOrderPassword), eq(encodedPassword))).thenReturn(true);

        OrderDTO mockOrder = new OrderDTO(
                123L,
                30000,
                LocalDateTime.now(),
                null,
                new OrderStatusDTO(1L, "WAIT"),
                "12345",
                "광주",
                "조선대",
                null
        );
        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ok(mockOrder));

        OrderBookResponseDTO[] mockOrderBooks = new OrderBookResponseDTO[] {
                new OrderBookResponseDTO(1L, 1L, "1", 1, 10000, null),
                new OrderBookResponseDTO(2L, 2L, "2", 1, 20000, null)
        };
        when(orderBookAdapter.getOrderBooksByOrderId(orderId)).thenReturn(mockOrderBooks);

        when(deliveryAdapter.getDelivery(orderId)).thenReturn(new DeliveryDTO(
                123L, 0, LocalDateTime.now(), LocalDateTime.now(), new DeliveryDTO.OrderDTO(123L)
        ));
        when(returnsReasonAdapter.getAllReturnsReasons()).thenReturn(List.of(new ReturnsReasonDTO(1L, "Reason1")));

        mockMvc.perform(post("/guestOrder")
                        .param("orderId", orderId.toString())
                        .param("guestOrderPassword", guestOrderPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/guestOrderInfo"))
                .andExpect(model().attributeExists("order", "orderBookLists", "amountPrice",
                        "discountPrice", "paymentAmountBookPrice",
                        "wrappingPaperPrice", "deliveryCost", "returnsReasonList"));
    }

    @Test
    @DisplayName("비회원 주문 조회 페이지 실패")
    void checkGuestOrder_WithInvalidPassword_ShouldRedirectToGuestOrderLogin() throws Exception {
        Long orderId = 123L;
        String guestOrderPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        // Mocking
        when(passwordEncoder.encode(guestOrderPassword)).thenReturn(encodedPassword);
        when(orderAdapter.getGuestOrderPassword(any(GuestOrderValidateRequestDTO.class)))
                .thenReturn(ResponseEntity.ok(encodedPassword));
        when(passwordEncoder.matches(eq(guestOrderPassword), eq(encodedPassword))).thenReturn(false);

        mockMvc.perform(post("/guestOrder")
                        .param("orderId", orderId.toString())
                        .param("guestOrderPassword", guestOrderPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/guestOrder"))
                .andExpect(flash().attribute("error", "주문 정보가 올바르지 않습니다."));
    }
}