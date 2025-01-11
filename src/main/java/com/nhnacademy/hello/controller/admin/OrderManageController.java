package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/orders")
public class OrderManageController {

    private final OrderBookAdapter orderBookAdapter;
    private final OrderAdapter orderAdapter;
    private final BookAdapter bookAdapter;
    private final MemberAdapter memberAdapter;

    @GetMapping
    public String getOrders(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        ResponseEntity<List<OrderDTO>> response = orderAdapter.getAllOrders(page - 1);
        List<OrderDTO> orders = response.getBody();

        if (orders != null && !orders.isEmpty()) {
            orders = orders.stream()
                    .map(order -> {
                        OrderDTO.MemberDTO member = order.member();
                        String memberId = "비회원";
                        String contact = "Unknown";
                        String email = "Unknown";

                        if (member == null) {
                            try {
                                GuestOrderDTO guestOrder = orderAdapter.getGuestOrder(order.orderId());
                                contact = guestOrder.guestOrderNumber();
                                email = guestOrder.guestOrderEmail();
                            } catch (FeignException.NotFound e) {
                                contact = "Unknown";
                                email = "Unknown";
                            }
                        } else {
                            memberId = member.memberId();
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
                                new OrderDTO.MemberDTO(memberId, member != null ? member.memberName() : null, contact, email)
                        );
                    }).collect(Collectors.toList());
        } else {
            orders = List.of();
        }

        model.addAttribute("orders", orders);

        ResponseEntity<Long> totalOrderCountResponse = orderAdapter.getTotalOrderCount();
        Long totalOrders = totalOrderCountResponse.getBody();
        int totalPages = (totalOrders == null) ? 0 : (int) Math.ceil((double) totalOrders / pageSize);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/orderManage";
    }

    @PostMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

            if (order != null) {
                Long statusId = null;

                // 상태 이름에 따라 ID 매핑
                switch (status.toUpperCase()) {
                    case "ON_DELIVERY":
                        statusId = 2L; // 배송중 상태 ID
                        break;
                    case "COMPLETE":
                        statusId = 3L; // 주문완료 상태 ID
                        break;
                    default:
                        return ResponseEntity.badRequest().build();
                }

                OrderRequestDTO updatedOrder = new OrderRequestDTO(
                        order.member() != null ? order.member().memberId() : "Unknown",
                        order.orderPrice(),
                        order.wrappingPaper() != null ? order.wrappingPaper().wrappingPaperId() : null,
                        statusId,
                        order.zoneCode(),
                        order.address(),
                        order.addressDetail()
                );
                orderAdapter.updateOrder(orderId, updatedOrder);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{orderId}/process")
    @ResponseBody
    public ResponseEntity<Void> processOrder(@PathVariable Long orderId) {
        return updateOrderStatus(orderId, Map.of("status", "ONDELIVERY"));
    }

    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();
        List<OrderBookResponseDTO> books = List.of();
        GuestOrderDTO guestOrder = null;

        if (order != null) {
            OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(orderId);
            if (orderBooks != null) {
                books = Arrays.asList(orderBooks);
            }

            OrderDTO.MemberDTO member = order.member();
            if (member == null) {
                try {
                    guestOrder = orderAdapter.getGuestOrder(orderId);
                } catch (FeignException.NotFound e) {
                    guestOrder = new GuestOrderDTO(orderId, "Unknown", "Unknown");
                }
            } else {
                try {
                    MemberDTO fullMemberInfo = memberAdapter.getMember(member.memberId());
                    member = new OrderDTO.MemberDTO(
                            fullMemberInfo.memberId(),
                            fullMemberInfo.memberName() != null ? fullMemberInfo.memberName() : "Unknown",
                            fullMemberInfo.memberNumber() != null ? fullMemberInfo.memberNumber() : "Unknown",
                            fullMemberInfo.memberEmail() != null ? fullMemberInfo.memberEmail() : "Unknown"
                    );
                } catch (FeignException.NotFound e) {
                    member = new OrderDTO.MemberDTO(
                            member.memberId(),
                            "Unknown",
                            "Unknown",
                            "Unknown"
                    );
                }
            }

            order = new OrderDTO(
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

        model.addAttribute("order", order);
        model.addAttribute("books", books);
        model.addAttribute("guestOrder", guestOrder);

        return "admin/orderDetail";
    }
}