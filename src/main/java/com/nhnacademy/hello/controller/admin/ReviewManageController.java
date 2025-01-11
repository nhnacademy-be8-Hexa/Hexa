package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberReportAdapter;
import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.dto.review.ReviewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/review")
@Slf4j
public class ReviewManageController {
    private final ReviewAdapter reviewAdapter;
    private final MemberReportAdapter memberReportAdapter;

    // 신고 5회 이상 받은 리뷰 전체 조회
    @GetMapping
    public String getReportedReviews(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "9") int size,
                                     @RequestParam(required = false) String sort,
                                     @RequestParam(required = false) String search,
                                     Model model) {
        try {
            int adjustedPage = Math.max(page - 1, 0);

            // 리뷰 목록 가져오기
            ResponseEntity<List<ReviewDTO>> response = reviewAdapter.getReviewsFromHighReport(adjustedPage, size, sort);
            List<ReviewDTO> reviews = response.getBody();
            if (reviews == null) {
                reviews = List.of(); // 빈 리스트로 초기화
            }

            // 총 리뷰 수 가져오기
            long totalReportedReviews = 0;
            try {
                ResponseEntity<Long> totalResponse = reviewAdapter.getReviewsFromHighReportTotal();
                if (totalResponse != null && totalResponse.getBody() != null) {
                    totalReportedReviews = totalResponse.getBody();
                }
            } catch (Exception e) {
                log.error("Error fetching total reported reviews: {}", e.getMessage());
            }

            int totalPages = (int) Math.ceil((double) totalReportedReviews / size);

            model.addAttribute("reviews", reviews);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", totalPages);
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("search", search);

            return "admin/reviewManage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 데이터를 가져오는 중 문제가 발생했습니다.");
            return "error/500"; // 에러 페이지로 이동
        }
    }


    // 특정 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public String getReviewDetail(@PathVariable Long reviewId, Model model) {
        Optional<List<ReviewDTO>> reviews = Optional.ofNullable(reviewAdapter.getReviewsFromAdmin(0, 1, "reviewId").getBody());
        if (reviews.isPresent()) {
            for (ReviewDTO review : reviews.get()) {
                if (review.reviewId().equals(reviewId)) {
                    model.addAttribute("review", review);
                    return "admin/reviewDetail";
                }
            }
        }
        return "redirect:/admin/review";
    }

    // 특정 리뷰 수정
    @PutMapping("/{reviewId}/block")
    @ResponseBody
    public ResponseEntity<Void> blockReview(@PathVariable Long reviewId, @RequestParam boolean block) {
        memberReportAdapter.allDelete(reviewId);
        return reviewAdapter.updateReviewBlock(reviewId, block);
    }
}
