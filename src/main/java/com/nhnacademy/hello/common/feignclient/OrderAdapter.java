package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.order.GuestOrderDTO;
import com.nhnacademy.hello.dto.order.GuestOrderRequestDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "orderAdapter")
public interface OrderAdapter {

    @PostMapping("/api/orders")
    public ResponseEntity<Long> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            @RequestParam List<Long> bookIds,
            @RequestParam List<Integer> amounts,
            @RequestParam(required = false) Long couponId);

    @GetMapping("/api/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page);

    @GetMapping("/api/members/{memberId}/orders")
    public ResponseEntity<List<OrderDTO>> getOrdersByMemberId(
            @Valid @RequestParam(defaultValue = "0") int page,
            @PathVariable String memberId);

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long orderId);

    @PutMapping("/api/orders/{orderId}")
    public ResponseEntity<Void> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequestDTO orderRequestDTO);

    // 특정 주문의 주문한 책 수량
    @GetMapping("api/orders/{orderId}/books/{bookId}")
    public ResponseEntity<Long> getOrderAmount(
            @PathVariable Long orderId,
            @PathVariable Long bookId);


    // 게스트 ------------------------------------------------
    @PostMapping("/api/guestOrders")
    public ResponseEntity<GuestOrderDTO> createGuestOrder(
            @Valid @RequestBody GuestOrderRequestDTO guestOrderRequestDTO);

    @GetMapping("/api/guestOrders")
    public List<GuestOrderDTO> getAllGuestOrders(
            @RequestParam(defaultValue = "0") int page);

    @GetMapping("/api/guestOrders/{orderId}")
    public GuestOrderDTO getGuestOrder(
            @PathVariable Long orderId);

    @PutMapping("/api/guestOrders")
    public ResponseEntity<GuestOrderDTO> updateGuestOrder(
            @Valid @RequestBody GuestOrderRequestDTO guestOrderRequestDTOs);

    @GetMapping("/api/orders/count")
    public ResponseEntity<Long> getTotalOrderCount();
}
