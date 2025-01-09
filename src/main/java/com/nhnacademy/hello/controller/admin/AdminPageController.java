package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.OrderStatusAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminPageController {
    private final MemberAdapter memberAdapter;
    private final BookAdapter bookAdapter;
    private final OrderAdapter orderAdapter;
    private final OrderStatusAdapter orderStatusAdapter;

    @GetMapping("/admin")
    public String adminPage(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        if (!AuthInfoUtils.isLogin()) {
            return "redirect:/login";
        }

        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());
        if (!"ADMIN".equals(memberDTO.memberRole())) {
            return "redirect:/index";
        }
        model.addAttribute("member", memberDTO);

        // 가장 많이 방문한 도서 Top 5 조회
        List<BookDTO> mostVisitedBooks = bookAdapter.getBooks(0, 5, "", null, null, null, null, true, null, null, null, null, null, null);
        Map<Long, List<AuthorDTO>> bookAuthorsMap = new HashMap<>();
        Map<Long, Long> bookLikesMap = new HashMap<>();

        for (BookDTO book : mostVisitedBooks) {
            List<AuthorDTO> authors = bookAdapter.getAuthors(book.bookId());
            bookAuthorsMap.put(book.bookId(), authors);

            Long likeCount = bookAdapter.getLikeCount(book.bookId()).getBody();
            bookLikesMap.put(book.bookId(), likeCount);
        }

        model.addAttribute("mostVisitedBooks", mostVisitedBooks);
        model.addAttribute("bookAuthorsMap", bookAuthorsMap);
        model.addAttribute("bookLikesMap", bookLikesMap);


        // 주문 목록 조회
        ResponseEntity<List<OrderDTO>> response = orderAdapter.getAllOrders(page - 1);
        List<OrderDTO> allOrders = response.getBody();

        if (allOrders != null && !allOrders.isEmpty()) {
            List<OrderDTO> pendingOrders = allOrders.stream()
                    .filter(order -> "WAIT".equalsIgnoreCase(order.orderStatus().orderStatus()))
                    .map(order -> {
                        if (order.member() == null) {
                            return new OrderDTO(
                                    order.orderId(),
                                    order.orderPrice(),
                                    order.orderedAt(),
                                    order.wrappingPaper(),
                                    order.orderStatus(),
                                    order.zoneCode(),
                                    order.address(),
                                    order.addressDetail(),
                                    new OrderDTO.MemberDTO("Unknown", "Unknown"));
                        }
                        return order;
                    })
                    .toList();

            model.addAttribute("orders", pendingOrders);
        } else {
            model.addAttribute("orders", List.of());
        }

        ResponseEntity<Long> totalOrderCountResponse = orderAdapter.getTotalOrderCount();
        Long totalOrders = totalOrderCountResponse.getBody();
        int totalPages = (totalOrders == null) ? 0 : (int) Math.ceil((double) totalOrders / pageSize);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);



        return "admin/adminPage";
    }

}
