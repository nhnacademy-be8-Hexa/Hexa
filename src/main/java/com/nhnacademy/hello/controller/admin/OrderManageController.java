package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.OrderBookAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
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
    private final BookAdapter bookAdapter;
    private final MemberAdapter memberAdapter;

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

        if (order != null) {
            // 주문서에 포함된 도서 정보 가져오기
            OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(orderId);

            List<BookDTO> books = List.of();
            if (orderBooks != null && orderBooks.length > 0) {
                List<Long> bookIds = Arrays.stream(orderBooks)
                        .map(OrderBookResponseDTO::bookId)
                        .collect(Collectors.toList());
                books = bookAdapter.getBooksByIds(bookIds); // 도서 정보 조회
            }

            // 회원 정보 가져오기
            OrderDTO.MemberDTO member = order.member();
            if (member != null && !"Unknown".equals(member.memberId())) {
                MemberDTO memberDTO = memberAdapter.getMember(member.memberId());
                if (memberDTO != null) {
                    // 회원 정보를 OrderDTO의 MemberDTO로 변환
                    member = new OrderDTO.MemberDTO(
                            memberDTO.memberId(),
                            memberDTO.memberName(),
                            memberDTO.memberNumber()
                    );
                }
            }

            // 최종 OrderDTO 생성
            order = new OrderDTO(
                    order.orderId(),
                    order.orderPrice(),
                    order.orderedAt(),
                    order.wrappingPaper(),
                    order.orderStatus(),
                    order.zoneCode(),
                    order.address(),
                    order.addressDetail(),
                    member != null ? member : new OrderDTO.MemberDTO("Unknown", "Unknown", "Unknown"),
                    books
            );
        }

        model.addAttribute("order", order);
        return "admin/orderDetail";
    }
}
