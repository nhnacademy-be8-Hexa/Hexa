package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.review.ReviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MemberReviewController {
    private final ReviewAdapter reviewAdapter;

    @GetMapping("/mypage/reviews")
    public String memberReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String sort,
            Model model) {
        String memberId = AuthInfoUtils.getUsername();
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;
        model.addAttribute("memberId", memberId);
        List<ReviewDTO> reviews = reviewAdapter.getReviewsFromMember(memberId, adjustedPage, size, sort).getBody();
        model.addAttribute("reviews", reviews);

        long totalPage = reviewAdapter.getTotalReviews(memberId).getBody();

        int totalPages = (int) Math.ceil((double) totalPage / size);

        model.addAttribute("totalPage", totalPages);
        model.addAttribute("currentPage", adjustedPage);
        model.addAttribute("page", adjustedPage);
        return "member/myReview";
    }
}
