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

import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/orders")
public class OrderManageController {

    private final OrderStatusAdapter orderStatusAdapter;
    private final OrderAdapter orderAdapter;

    @PostMapping("/{orderId}/update-status")
    @ResponseBody
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId,
                                                  @RequestBody Map<String, String> body) {
        Long statusId = Long.valueOf(body.get("statusId"));
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

        OrderRequestDTO updatedOrder = new OrderRequestDTO(
                order.member().memberId(),
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

    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();
        model.addAttribute("order", order);
        return "admin/orderManage";
    }

}
