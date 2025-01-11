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
import org.springframework.http.ResponseEntity;
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

        // 전체 주문 데이터 가져오기
        ResponseEntity<List<OrderDTO>> response = orderAdapter.getAllOrders(page - 1);
        List<OrderDTO> paginatedOrders = response != null && response.getBody() != null ? response.getBody() : List.of();

        // 주문 상태가 "WAIT"인 주문만 가져오고 회원/비회원 정보 처리
        List<Map<String, Object>> enrichedOrders = paginatedOrders.stream()
                .filter(order -> "WAIT".equalsIgnoreCase(order.orderStatus().orderStatus()))
                .map(order -> {
                    Map<String, Object> orderDetails = new HashMap<>();
                    orderDetails.put("order", order);

                    // 회원 정보 처리
                    OrderDTO.MemberDTO member = order.member();
                    if (member == null || "Unknown".equals(member.memberId())) {
                        try {
                            GuestOrderDTO guestOrder = orderAdapter.getGuestOrder(order.orderId());
                            member = new OrderDTO.MemberDTO(
                                    "비회원",
                                    "Unknown",
                                    guestOrder.guestOrderNumber(),
                                    guestOrder.guestOrderEmail()
                            );
                        } catch (FeignException.NotFound e) {
                            member = new OrderDTO.MemberDTO(
                                    "비회원",
                                    "Unknown",
                                    "Unknown",
                                    "Unknown"
                            );
                        }
                    } else {
                        try {
                            MemberDTO fullMemberInfo = memberAdapter.getMember(member.memberId());
                            logger.info("회원 데이터: {}", fullMemberInfo);

                            member = new OrderDTO.MemberDTO(
                                    fullMemberInfo.memberId(),
                                    fullMemberInfo.memberName() != null ? fullMemberInfo.memberName() : "알 수 없음",
                                    fullMemberInfo.memberNumber(),
                                    fullMemberInfo.memberEmail()
                            );
                        } catch (FeignException.NotFound e) {
                            logger.warn("회원 정보를 가져오지 못했습니다. 회원 ID: {}", member.memberId(), e);
                            member = new OrderDTO.MemberDTO(
                                    member.memberId(),
                                    "Unknown",
                                    "Unknown",
                                    "Unknown"
                            );
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
                })
                .collect(Collectors.toList());

        model.addAttribute("orders", enrichedOrders);

        // 페이징 계산
        ResponseEntity<Long> totalOrderCountResponse = orderAdapter.getTotalOrderCount();
        long totalOrders = totalOrderCountResponse != null && totalOrderCountResponse.getBody() != null ? totalOrderCountResponse.getBody() : 0L;
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/adminPage";
    }
}