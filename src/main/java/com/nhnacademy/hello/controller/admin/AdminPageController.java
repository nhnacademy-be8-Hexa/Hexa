package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
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
public class  AdminPageController {
    private final MemberAdapter memberAdapter;
    private final BookAdapter bookAdapter;
    private final OrderAdapter orderAdapter;


    @GetMapping("/admin")
    public String adminPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model
    ) {
        if (!AuthInfoUtils.isLogin()) {
            return "redirect:/login";
        }

        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        // 관리자인지 검사
        if (!"ADMIN".equals(memberDTO.memberRole())) {
            return "redirect:/index";
        }

        model.addAttribute("member", memberDTO);

        // 가장 많이 방문한 도서 Top 5 조회
        List<BookDTO> mostVisitedBooks = bookAdapter.getBooks(
                0, 5, "", null, null, null, null, true, null, null, null, null, null, null
        );

        // 각 도서의 저자 및 좋아요 수 가져오기
        Map<Long, List<AuthorDTO>> bookAuthorsMap = new HashMap<>();
        Map<Long, Long> bookLikesMap = new HashMap<>();

        for (BookDTO book : mostVisitedBooks) {
            // 저자 정보 가져오기
            List<AuthorDTO> authors = bookAdapter.getAuthors(book.bookId());
            bookAuthorsMap.put(book.bookId(), authors);

            // 좋아요 수 가져오기
            Long likeCount = bookAdapter.getLikeCount(book.bookId()).getBody();
            bookLikesMap.put(book.bookId(), likeCount);
        }

        model.addAttribute("mostVisitedBooks", mostVisitedBooks);
        model.addAttribute("bookAuthorsMap", bookAuthorsMap);
        model.addAttribute("bookLikesMap", bookLikesMap);

        // 배송 상태 관리: 주문 목록 불러오기
        ResponseEntity<List<OrderDTO>> response = orderAdapter.getAllOrders(page);
        List<OrderDTO> orders = response.getBody();

        // 총 페이지 계산
        int totalOrders = 100; // 실제 총 주문 수를 가져오는 API가 필요
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/adminPage"; // 템플릿 파일 경로
    }

}