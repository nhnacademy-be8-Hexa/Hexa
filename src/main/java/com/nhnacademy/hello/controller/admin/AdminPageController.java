package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.GuestOrderDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminPageController {
    private static final Logger logger = LoggerFactory.getLogger(AdminPageController.class);

    private final MemberAdapter memberAdapter;
    private final BookAdapter bookAdapter;
    private final OrderAdapter orderAdapter;
    private final OrderBookAdapter orderBookAdapter;

    @GetMapping("/admin")
    public String adminPage(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        // 관리자 인증
        if (!AuthInfoUtils.isLogin()) {
            return "redirect:/login";
        }

        MemberDTO adminMember = memberAdapter.getMember(AuthInfoUtils.getUsername());
        if (!"ADMIN".equals(adminMember.memberRole())) {
            return "redirect:/index";
        }
        model.addAttribute("member", adminMember);

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

        // 주문 대기 목록 추가
        addOrderDataToModel(page, pageSize, "waitOrders", 1L, model);

        // 환불 대기 목록 추가
        addOrderDataToModel(page, pageSize, "returnOrders", 6L, model);

        return "admin/adminPage";
    }

    private void addOrderDataToModel(
            int page, int pageSize, String attributeName, Long statusId, Model model) {
        try {
            // 상태별 주문 목록 가져오기
            List<OrderDTO> orders = orderAdapter.getOrderStatus(statusId, page - 1, pageSize);

            // 상태별 총 개수 가져오기
            Long totalCount = orderAdapter.countOrdersByStatus(statusId).getBody();

            // 페이징 계산
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            int currentPage = Math.min(Math.max(page, 1), totalPages);

            // 상세 정보 구성
            List<Map<String, Object>> enrichedOrders = enrichOrderDetails(orders);

            model.addAttribute(attributeName, enrichedOrders);
            model.addAttribute(attributeName + "CurrentPage", currentPage);
            model.addAttribute(attributeName + "TotalPages", totalPages);
        } catch (Exception e) {
            logger.error(attributeName + " 데이터를 가져오는 중 오류 발생", e);
            model.addAttribute(attributeName, List.of());
            model.addAttribute(attributeName + "CurrentPage", 1);
            model.addAttribute(attributeName + "TotalPages", 0);
        }
    }

    private List<Map<String, Object>> enrichOrderDetails(List<OrderDTO> orders) {
        return orders.stream().map(order -> {
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("order", order);

            // 회원 정보 처리
            OrderDTO.MemberDTO member = order.member();
            if (member == null || "Unknown".equals(member.memberId())) {
                try {
                    GuestOrderDTO guestOrder = orderAdapter.getGuestOrder(order.orderId());
                    member = new OrderDTO.MemberDTO(
                            "비회원", "Unknown",
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
            orderDetails.put("member", member);

            // 도서 정보 처리
            try {
                OrderBookResponseDTO[] orderBooks = orderBookAdapter.getOrderBooksByOrderId(order.orderId());
                orderDetails.put("books", Arrays.asList(orderBooks));
            } catch (FeignException.NotFound e) {
                orderDetails.put("books", List.of());
            }

            return orderDetails;
        }).collect(Collectors.toList());
    }
}