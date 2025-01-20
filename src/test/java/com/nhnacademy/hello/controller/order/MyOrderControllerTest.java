package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyOrderController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class MyOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderAdapter orderAdapter;

    @MockitoBean
    private OrderBookAdapter orderBookAdapter;

    @MockitoBean
    private MemberAdapter memberAdapter;

    @MockitoBean
    private DeliveryAdapter deliveryAdapter;

    @MockitoBean
    private ReturnsReasonAdapter returnsReasonAdapter;

    @MockitoBean
    private ReturnsAdapter returnsAdapter;

    private MemberDTO mockMember;
    private List<OrderDTO> mockOrdersList;
    private OrderDTO mockOrder;
    private OrderBookResponseDTO[] mockOrderBooks;
    private ReturnsDTO mockReturns;

    @BeforeEach
    void setUp() {
        mockMember = new MemberDTO("user",
                "testUser",
                "01012345678",
                "test@example.com",
                LocalDate.now(),
                LocalDate.now(),
                LocalDateTime.now(),
                "ROLE_MEMBER",
                new RatingDTO(1L,"normal", 10),
                new MemberStatusDTO(1L, "active")
        );

        when(memberAdapter.getMember(anyString())).thenReturn(mockMember);

        mockOrder = new OrderDTO(1L,
                10000,
                LocalDateTime.now(),
                null,
                new OrderStatusDTO(1L, "WAIT"),
                "12345",
                "광주",
                "조선대",
                new OrderDTO.MemberDTO("user", "testUser", "01012345678", "test@example.com")
        );

        when(orderAdapter.getOrderById(1L)).thenReturn(ResponseEntity.ok(mockOrder));

        mockOrdersList = Collections.singletonList(mockOrder);

        when(orderAdapter.countAllByMember_MemberId(anyString())).thenReturn(new ResponseEntity<>(100L, HttpStatus.OK));
        when(orderAdapter.getOrdersByMemberId(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(new ResponseEntity<>(mockOrdersList, HttpStatus.OK));

        mockOrderBooks = List.of(new OrderBookResponseDTO(1L,
                1L,
                "1",
                1,
                10000,
                null)).toArray(new OrderBookResponseDTO[0]);

        mockReturns = new ReturnsDTO(1L,
                new ReturnsReasonDTO(1L, ""),
                "Test Detail",
                null);

    }

    @Test
    @DisplayName("내 주문 내역 조회 페이지")
    @WithMockUser(username = "user", roles = "MEMBER")
    public void testOrdersPage() throws Exception {

        // when & then
        mockMvc.perform(get("/mypage/orders?page=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/orders"))
                .andExpect(model().attributeExists("ordersList"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    @DisplayName("주문 상세 페이지")
    @WithMockUser(username = "user", roles = "MEMBER")
    public void testOrdersPageWithOrderId() throws Exception {
        // given

        when(memberAdapter.getMember(anyString())).thenReturn(mockMember);
        when(orderAdapter.existsOrderIdAndMember_MemberId(anyLong(), anyString())).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        when(orderAdapter.getOrderById(anyLong())).thenReturn(new ResponseEntity<>(mockOrder, HttpStatus.OK));
        when(orderBookAdapter.getOrderBooksByOrderId(anyLong())).thenReturn(mockOrderBooks);
        when(returnsAdapter.getReturnsByOrderId(anyLong())).thenReturn(mockReturns);

        // when & then
        mockMvc.perform(get("/mypage/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("member/orderBooks"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("orderBookLists"))
                .andExpect(model().attributeExists("amountPrice"));
    }

    @Test
    @WithMockUser(username = "user", roles = "MEMBER")
    public void testCancelRefundPage() throws Exception {
        // given
        List<OrderDTO> mockRefundList = Collections.singletonList(mockOrder);
        List<OrderDTO> mockCancelList = Collections.singletonList(mockOrder);

        when(memberAdapter.getMember(anyString())).thenReturn(mockMember);
        when(orderAdapter.getOrdersByMemberId(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(new ResponseEntity<>(mockRefundList, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(mockCancelList, HttpStatus.OK));

        // when & then
        mockMvc.perform(get("/mypage/cancel-refunds?page=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/orderCancelReturn"))
                .andExpect(model().attributeExists("refundList"))
                .andExpect(model().attributeExists("cancelList"));
    }

}