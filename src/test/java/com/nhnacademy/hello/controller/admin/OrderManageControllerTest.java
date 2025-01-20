package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

class OrderManageControllerTest {

    @Mock
    private OrderBookAdapter orderBookAdapter;
    @Mock
    private OrderAdapter orderAdapter;
    @Mock
    private ReturnsAdapter returnsAdapter;
    @Mock
    private MemberAdapter memberAdapter;
    @Mock
    private OrderStatusAdapter orderStatusAdapter;
    @Mock
    private Model model;

    @InjectMocks
    private OrderManageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("주문 목록 가져오기 - 성공 (전체 주문)")
    void getOrders_Success_AllOrders() {
        // Given
        int page = 1; // 기본값
        int pageSize = 10;
        String sort = "orderId,desc";
        List<OrderDTO> orders = List.of(
                new OrderDTO(1L, 1000, null, null, null, null, null, null, null)
        );
        List<OrderStatusDTO> statuses = List.of(
                new OrderStatusDTO(1L, "PENDING"),
                new OrderStatusDTO(2L, "COMPLETED")
        );

        when(orderStatusAdapter.getAllOrderStatus()).thenReturn(statuses);
        when(orderAdapter.getAllOrders(eq(page - 1), eq(sort))).thenReturn(ResponseEntity.ok(orders));
        when(orderAdapter.getTotalOrderCount()).thenReturn(ResponseEntity.ok(1L));

        // When
        String viewName = controller.getOrders(page, pageSize, null, model);

        // Then
        assertEquals("admin/orderManage", viewName);
        verify(model).addAttribute("statuses", statuses);
        verify(model).addAttribute("orders", orders);
        verify(model).addAttribute("currentPage", page);
        verify(model).addAttribute("totalPages", 1);
        verify(model).addAttribute("statusId", null);
    }

    @Test
    @DisplayName("주문 목록 가져오기 - 성공 (특정 상태 주문)")
    void getOrders_Success_SpecificStatus() {
        // Given
        int page = 1;
        int pageSize = 10;
        Long statusId = 1L;
        String sort = "orderId,desc";
        List<OrderDTO> orders = List.of(
                new OrderDTO(1L, 1000, null, null, null, null, null, null, null)
        );
        List<OrderStatusDTO> statuses = List.of(
                new OrderStatusDTO(1L, "PENDING"),
                new OrderStatusDTO(2L, "COMPLETED")
        );

        when(orderStatusAdapter.getAllOrderStatus()).thenReturn(statuses);
        when(orderAdapter.getOrderStatus(eq(statusId), eq(page - 1), eq(pageSize), eq(sort))).thenReturn(orders);
        when(orderAdapter.countOrdersByStatus(eq(statusId))).thenReturn(ResponseEntity.ok(1L));

        // When
        String viewName = controller.getOrders(page, pageSize, statusId, model);

        // Then
        assertEquals("admin/orderManage", viewName);
        verify(model).addAttribute("statuses", statuses);
        verify(model).addAttribute("orders", orders);
        verify(model).addAttribute("currentPage", page);
        verify(model).addAttribute("totalPages", 1);
        verify(model).addAttribute("statusId", statusId);
    }

    @Test
    @DisplayName("주문 목록 가져오기 - 예외 처리")
    void getOrders_ExceptionHandling() {
        // Given
        int page = 1;
        int pageSize = 10;
        List<OrderStatusDTO> statuses = List.of(
                new OrderStatusDTO(1L, "PENDING"),
                new OrderStatusDTO(2L, "COMPLETED")
        );

        when(orderStatusAdapter.getAllOrderStatus()).thenReturn(statuses);
        when(orderAdapter.getAllOrders(eq(page - 1), anyString())).thenThrow(new RuntimeException("Data fetch error"));

        // When
        String viewName = controller.getOrders(page, pageSize, null, model);

        // Then
        assertEquals("admin/orderManage", viewName);
        verify(model).addAttribute("statuses", statuses);
        verify(model).addAttribute("orders", List.of());
        verify(model).addAttribute("currentPage", page);
        verify(model).addAttribute("totalPages", 0);
    }

    @Test
    @DisplayName("주문 상세 조회 - 성공")
    void getOrderDetail_Success() {
        // Given
        Long orderId = 1L;

        OrderDTO.MemberDTO member = new OrderDTO.MemberDTO("user1", "User Name", "123-4567", "user1@example.com");
        OrderDTO originalOrder = new OrderDTO(
                orderId,
                1000,
                LocalDateTime.now(),
                null,
                new OrderStatusDTO(1L, "RETURN_REQUEST"),
                "12345",
                "서울시 강남구",
                "101호",
                member
        );

        OrderBookResponseDTO book = new OrderBookResponseDTO(1L, 101L, "Book1", 2, 5000, 1L);
        ReturnsDTO returns = new ReturnsDTO(
                orderId,
                new ReturnsReasonDTO(1L, "Damage"),
                "Broken spine",
                new ReturnsDTO.OrderDTO(orderId)
        );

        // Mock 설정
        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ok(originalOrder));
        when(orderBookAdapter.getOrderBooksByOrderId(orderId)).thenReturn(new OrderBookResponseDTO[]{book});
        when(returnsAdapter.getReturnsByOrderId(orderId)).thenReturn(returns);
        when(memberAdapter.getMember(member.memberId())).thenReturn(new MemberDTO(
                "user1",
                "User Name",
                "123-4567",
                "user1@example.com",
                null, null, null, null, null, null
        ));

        // When
        String viewName = controller.getOrderDetail(orderId, model);

        // Then
        assertEquals("admin/orderDetail", viewName);

        verify(model).addAttribute(eq("order"), argThat(order -> {
            if (!(order instanceof OrderDTO)) return false;
            OrderDTO orderDTO = (OrderDTO) order;
            return orderDTO.orderId().equals(originalOrder.orderId()) &&
                    orderDTO.member().memberId().equals(originalOrder.member().memberId());
        }));

        verify(model).addAttribute("books", List.of(book));
        verify(model).addAttribute("returnsReason", "Damage");
        verify(model).addAttribute("returnsDetail", "Broken spine");
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 성공")
    void updateOrderStatus_Success() {
        // Given
        Long orderId = 1L;
        Long statusId = 2L;
        OrderDTO order = new OrderDTO(orderId, 1000, null, null, null, null, null, null, null);

        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ok(order));

        // When
        ResponseEntity<Void> response = controller.updateOrderStatus(orderId, Map.of("statusId", statusId));

        // Then
        assertEquals(ResponseEntity.ok().build(), response);
        verify(orderAdapter).updateOrder(eq(orderId), any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("주문 상태 업데이트 - 실패 (주문 정보 없음)")
    void updateOrderStatus_Failure_OrderNotFound() {
        // Given
        Long orderId = 1L;
        Long statusId = 2L;

        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ofNullable(null));

        // When
        ResponseEntity<Void> response = controller.updateOrderStatus(orderId, Map.of("statusId", statusId));

        // Then
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    @DisplayName("주문 상세 조회 - 반품 정보 없음")
    void getOrderDetail_NoReturns() {
        // Given
        Long orderId = 1L;

        // 초기 OrderDTO 설정 (orderStatus는 null로 설정)
        OrderDTO initialOrder = new OrderDTO(orderId, 1000, null, null, null, null, null, null, null);
        OrderBookResponseDTO book = new OrderBookResponseDTO(1L, 101L, "Book1", 2, 5000, null);

        // Mock 설정
        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ok(initialOrder));
        when(orderBookAdapter.getOrderBooksByOrderId(orderId)).thenReturn(new OrderBookResponseDTO[]{book});
        when(returnsAdapter.getReturnsByOrderId(orderId)).thenReturn(null);

        GuestOrderDTO guestOrderDTO = new GuestOrderDTO(orderId, "guest-12345", "guest@example.com");
        when(orderAdapter.getGuestOrder(orderId)).thenReturn(guestOrderDTO);

        OrderDTO.MemberDTO mockMember = new OrderDTO.MemberDTO("비회원", "Unknown", "guest-12345", "guest@example.com");
        OrderDTO processedOrder = new OrderDTO(
                orderId,
                initialOrder.orderPrice(),
                initialOrder.orderedAt(),
                initialOrder.wrappingPaper(),
                null, // orderStatus는 null로 유지
                initialOrder.zoneCode(),
                initialOrder.address(),
                initialOrder.addressDetail(),
                mockMember
        );

        when(orderAdapter.getOrderById(orderId)).thenReturn(ResponseEntity.ok(processedOrder));

        // When
        String viewName = controller.getOrderDetail(orderId, model);

        // Then
        assertEquals("admin/orderDetail", viewName);

        verify(model).addAttribute(eq("order"), argThat(o -> {
            if (!(o instanceof OrderDTO)) return false;
            OrderDTO dto = (OrderDTO) o;
            return dto.orderId().equals(orderId)
                    && dto.member() != null
                    && "비회원".equals(dto.member().memberId());
        }));

        verify(model).addAttribute("books", List.of(book));
        verify(model).addAttribute("returnsReason", "Unknown");
        verify(model).addAttribute("returnsDetail", "Unknown");
    }
}
