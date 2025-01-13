package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                orders = orderAdapter.getOrderStatus(statusId, page - 1, pageSize);
                totalOrders = orderAdapter.countOrdersByStatus(statusId).getBody();
            } else {
                orders = orderAdapter.getAllOrders(page - 1).getBody();
                totalOrders = orderAdapter.getTotalOrderCount().getBody();
            }

            orders = orders.stream()
                    .map(this::processOrderMemberInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // 예외 발생 시에도 UI에 빈 리스트와 기본 정보를 제공
            model.addAttribute("orders", List.of());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 0);
            return "admin/orderManage";
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
        String returnsDetail = "Unknown";

        try {
            if (order != null) {
                OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(orderId);
                if (orderBooks != null) {
                    books = Arrays.asList(orderBooks);
                }

                String orderStatus = order.orderStatus().orderStatus();
                if ("RETURN_REQUEST".equalsIgnoreCase(orderStatus) || "RETURNED".equalsIgnoreCase(orderStatus)) {
                    ReturnsDTO returns = returnsAdapter.getReturnsByOrderId(orderId);
                    if (returns != null) {
                        if (returns.returnsReason() != null) {
                            returnsReason = returns.returnsReason().returnsReason();
                        }
                        if (returns.returnsDetail() != null) {
                            returnsDetail = returns.returnsDetail();
                        }
                    }
                }

                order = processOrderMemberInfo(order);
            }
        } catch (Exception e) {
            // 예외 발생 시 기본 정보를 설정
            model.addAttribute("order", null);
            model.addAttribute("books", List.of());
            model.addAttribute("guestOrder", null);
            model.addAttribute("returnsReason", "Unknown");
            model.addAttribute("returnsDetail", "Unknown");
            return "admin/orderDetail";
        }

        model.addAttribute("order", order);
        model.addAttribute("books", books);
        model.addAttribute("guestOrder", guestOrder);
        model.addAttribute("returnsReason", returnsReason);
        model.addAttribute("returnsDetail", returnsDetail);

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