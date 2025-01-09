package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.OrderStatusAdapter;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/orders")
public class OrderManageController {

    private final OrderStatusAdapter orderStatusAdapter;
    private final OrderAdapter orderAdapter;

    @GetMapping
    public String getOrders(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10")int pageSize,
                            Model model){
        // 전체 주문 목록 조회
        ResponseEntity<List<OrderDTO>> response = orderAdapter.getAllOrders(page - 1);
        List<OrderDTO> orders = response.getBody();

        if (orders != null && !orders.isEmpty()) {
            model.addAttribute("orders", orders);
        } else {
            model.addAttribute("orders", List.of());
        }

        ResponseEntity<Long> totalOrderCountResponse = orderAdapter.getTotalOrderCount();
        Long totalOrders = totalOrderCountResponse.getBody();
        int totalPages = (totalOrders == null) ? 0 : (int) Math.ceil((double) totalOrders / pageSize);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "admin/orderManage"; // 전체 주문 목록 페이지
    }

    @PostMapping("/{orderId}/update")
    @ResponseBody
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        orderAdapter.updateOrder(orderId, orderRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/process")
    @ResponseBody
    public ResponseEntity<Void> processOrder(@PathVariable Long orderId) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

        if (order != null && "WAIT".equalsIgnoreCase(order.orderStatus().orderStatus())) {
            OrderRequestDTO updatedOrder = new OrderRequestDTO(
                    order.member() != null ? order.member().memberId() : "Unknown",
                    order.orderPrice(),
                    order.wrappingPaper() != null ? order.wrappingPaper().wrappingPaperId() : null,
                    2L, // 배송중 상태 ID
                    order.zoneCode(),
                    order.address(),
                    order.addressDetail()
            );
            orderAdapter.updateOrder(orderId, updatedOrder);
        }
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

        // Null 검사를 추가하여 기본값 설정
        if (order != null) {
            if (order.member() == null) {
                order = new OrderDTO(
                        order.orderId(),
                        order.orderPrice(),
                        order.orderedAt(),
                        order.wrappingPaper(),
                        order.orderStatus(),
                        order.zoneCode(),
                        order.address(),
                        order.addressDetail(),
                        new OrderDTO.MemberDTO("Unknown", "Unknown", "Unknown"), // 기본값 설정
                        order.books() != null ? order.books() : List.of() // Null 처리
                );
            } else if (order.books() == null) {
                order = new OrderDTO(
                        order.orderId(),
                        order.orderPrice(),
                        order.orderedAt(),
                        order.wrappingPaper(),
                        order.orderStatus(),
                        order.zoneCode(),
                        order.address(),
                        order.addressDetail(),
                        order.member(),
                        List.of() // Null 처리
                );
            }
        }
        model.addAttribute("order", order);
        return "admin/orderDetail";
    }
}
