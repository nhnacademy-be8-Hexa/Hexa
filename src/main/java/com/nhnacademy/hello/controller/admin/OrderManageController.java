package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
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
    private final ReturnsAdapter returnsAdapter;
    private final MemberAdapter memberAdapter;
    private final OrderStatusAdapter orderStatusAdapter;

    @GetMapping
    public String getOrders(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            @RequestParam(required = false) Long statusId,
                            Model model) {
        List<OrderDTO> orders = List.of();
        List<OrderStatusDTO> statuses = List.of();
        Long totalOrders = 0L;

        try {
            // 상태 목록 가져오기
            statuses = orderStatusAdapter.getAllOrderStatus();

            if (statusId != null) {
                // 상태 ID에 따른 주문 목록 가져오기
                orders = orderAdapter.getOrderStatus(statusId, page - 1, pageSize);
                totalOrders = orderAdapter.countOrdersByStatus(statusId).getBody();
            } else {
                // 모든 주문 목록 가져오기
                orders = orderAdapter.getAllOrders(page - 1).getBody();
                totalOrders = orderAdapter.getTotalOrderCount().getBody();
            }

        } catch (Exception e) {
            model.addAttribute("statuses", statuses);
            model.addAttribute("orders", List.of());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 0);
            return "admin/orderManage";
        }

        int totalPages = (totalOrders == null) ? 0 : (int) Math.ceil((double) totalOrders / pageSize);
        model.addAttribute("statuses", statuses);
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("statusId", statusId);

        return "admin/orderManage";
    }

    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        OrderDTO order = null;
        List<OrderBookResponseDTO> books = List.of();
        GuestOrderDTO guestOrder = null;
        String returnsReason = "Unknown";
        String returnsDetail = "Unknown";

        try {
            // Order 정보 가져오기
            order = orderAdapter.getOrderById(orderId).getBody();
            if (order != null) {
                // OrderBook 정보 가져오기
                OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(orderId);
                if (orderBooks != null) {
                    books = Arrays.asList(orderBooks);
                }

                // 반품 상태 처리
                if (order.orderStatus() != null && order.orderStatus().orderStatus() != null) {
                    String orderStatus = order.orderStatus().orderStatus();
                    if ("RETURN_REQUEST".equalsIgnoreCase(orderStatus) || "RETURNED".equalsIgnoreCase(orderStatus)) {
                        ReturnsDTO returns = returnsAdapter.getReturnsByOrderId(orderId);
                        if (returns != null) {
                            returnsReason = returns.returnsReason() != null ? returns.returnsReason().returnsReason() : returnsReason;
                            returnsDetail = returns.returnsDetail() != null ? returns.returnsDetail() : returnsDetail;
                        }
                    }
                }

                // 멤버 정보 처리
                order = processOrderMemberInfo(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("order", order);
        model.addAttribute("books", books);
        model.addAttribute("guestOrder", guestOrder);
        model.addAttribute("returnsReason", returnsReason);
        model.addAttribute("returnsDetail", returnsDetail);

        return "admin/orderDetail";
    }

    protected OrderDTO processOrderMemberInfo(OrderDTO order) {
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

    @PostMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, Long> request) {
        try {
            Long statusId = request.get("statusId");
            if (statusId == null) {
                return ResponseEntity.badRequest().build(); // 필수 데이터 누락
            }

            OrderDTO order = orderAdapter.getOrderById(orderId).getBody();
            if (order == null) {
                return ResponseEntity.notFound().build(); // 주문 정보가 없는 경우
            }

            // OrderRequestDTO 생성
            OrderRequestDTO updatedOrder = new OrderRequestDTO(
                    null,
                    null,
                    null,
                    statusId, // 상태 ID
                    null,
                    null,
                    null
            );

            // 상태 업데이트
            orderAdapter.updateOrder(orderId, updatedOrder);

            return ResponseEntity.ok().build(); // 성공
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 기록
            return ResponseEntity.status(500).body(null); // 서버 오류
        }
    }
}