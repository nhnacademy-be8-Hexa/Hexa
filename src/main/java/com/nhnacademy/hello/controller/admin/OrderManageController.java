package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/orders")
public class OrderManageController {

    private final OrderBookAdapter orderBookAdapter;
    private final OrderAdapter orderAdapter;
    private final ReturnsAdapter returnsAdapter;
    private final MemberAdapter memberAdapter;
    private final ReturnsReasonAdapter returnsReasonAdapter;

    @GetMapping
    public String getOrders(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            @RequestParam(required = false) Long statusId,
                            Model model) {
        List<OrderDTO> orders = List.of();
        Long totalOrders = 0L;

        try {
            if (statusId != null) {
                // 특정 상태의 주문 목록과 개수 가져오기
                orders = orderAdapter.getOrderStatus(statusId, page - 1, pageSize);
                totalOrders = orderAdapter.countOrdersByStatus(statusId).getBody();
            } else {
                // 모든 주문 목록과 개수 가져오기
                orders = orderAdapter.getAllOrders(page - 1).getBody();
                totalOrders = orderAdapter.getTotalOrderCount().getBody();
            }

            // 주문 목록 처리
            assert orders != null;
            orders = orders.stream()
                    .map(this::processOrderMemberInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching orders", e);
        }

        model.addAttribute("orders", orders);

        int totalPages = (totalOrders == null) ? 0 : (int) Math.ceil((double) totalOrders / pageSize);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/orderManage";
    }

    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();
        List<OrderBookResponseDTO> books = List.of();
        GuestOrderDTO guestOrder = null;
        String returnsReason = "Unknown";
        Long returnsReasonId = null;

        try {
            // 도서 정보 가져오기
            if (order != null) {
                OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(orderId);
                if (orderBooks != null) {
                    books = Arrays.asList(orderBooks);
                }

                // 반품 사유 및 사유 ID 가져오기
                String orderStatus = order.orderStatus().orderStatus();
                if ("RETURN_REQUEST".equalsIgnoreCase(orderStatus) || "RETURNED".equalsIgnoreCase(orderStatus)) {
                    try {
                        ReturnsDTO returns = returnsAdapter.getReturnsByOrderId(orderId);
                        log.info("Fetched ReturnsDTO: {}", returns);

                        if (returns != null && returns.returnsReason() != null) {
                            returnsReasonId = returns.returnsReason().returnsReasonId();
                            returnsReason = returns.returnsReason().returnsReason();
                        } else {
                            log.warn("ReturnsDTO does not contain returnsReason for orderId: {}", orderId);
                        }
                    } catch (FeignException.NotFound e) {
                        log.warn("Returns not found for orderId: {}", orderId);
                    }
                }

                // 회원 정보 처리
                order = processOrderMemberInfo(order);
            }
        } catch (Exception e) {
            log.error("Error fetching order details for orderId: {}", orderId, e);
        }

        model.addAttribute("order", order);
        model.addAttribute("books", books);
        model.addAttribute("guestOrder", guestOrder);
        model.addAttribute("returnsReason", returnsReason);
        model.addAttribute("returnsReasonId", returnsReasonId);

        log.info("Order Details: {}", order);
        log.info("Returns Reason: {}", returnsReason);
        log.info("Returns Reason ID: {}", returnsReasonId);

        return "admin/orderDetail";
    }

    private OrderDTO processOrderMemberInfo(OrderDTO order) {
        OrderDTO.MemberDTO member = order.member();

        if (member == null) {
            try {
                GuestOrderDTO guestOrder = orderAdapter.getGuestOrder(order.orderId());
                member = new OrderDTO.MemberDTO(
                        "비회원",
                        "Unknown",
                        guestOrder.guestOrderNumber(),
                        guestOrder.guestOrderEmail()
                );
            } catch (FeignException.NotFound e) {
                member = new OrderDTO.MemberDTO("비회원", "Unknown", "Unknown", "Unknown");
            }
        } else {
            try {
                MemberDTO fullMemberInfo = memberAdapter.getMember(member.memberId());
                member = new OrderDTO.MemberDTO(
                        fullMemberInfo.memberId(),
                        fullMemberInfo.memberName() != null ? fullMemberInfo.memberName() : "알 수 없음",
                        fullMemberInfo.memberNumber(),
                        fullMemberInfo.memberEmail()
                );
            } catch (FeignException.NotFound e) {
                member = new OrderDTO.MemberDTO("Unknown", "Unknown", "Unknown", "Unknown");
            }
        }

        return new OrderDTO(
                order.orderId(),
                order.orderPrice(),
                order.orderedAt(),
                order.wrappingPaper(),
                order.orderStatus(),
                order.zoneCode(),
                order.address(),
                order.addressDetail(),
                member
        );
    }
}