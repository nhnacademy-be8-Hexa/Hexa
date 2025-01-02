package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.dto.review.ReviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/review")
public class ReviewManageController {
    private final ReviewAdapter reviewAdapter;

    // 신고 5회 이상 받은 리뷰 전체 조회
    @GetMapping
    public String getReportedReviews(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "9") int size,
                                     @RequestParam(required = false) String sort,
                                     @RequestParam (required = false) String search, Model model){
        // 페이지 번호 조정 (1 기반을 0 기반으로 변환)
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        ResponseEntity<List<ReviewDTO>> response = reviewAdapter.getReviewsFromHighReport(adjustedPage,size,sort);

        // 5번 이상 신고된 리뷰 총계
        Long totalReportedReviews = reviewAdapter.getReviewsFromHighReportTotal().getBody();

        // 전체 페이지 수
        int totalPages = (int) Math.ceil((double) totalReportedReviews / size);

        model.addAttribute("reviews",response.getBody());
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPage",totalPages);
        model.addAttribute("size",size);
        model.addAttribute("sort",sort);

        return "admin/reviewManage";
    }

    // 특정 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public ReviewDTO getReportedReviewDetail(@PathVariable Long reviewId){

        List<ReviewDTO> reviews = reviewAdapter.getReviewsFromAdmin(0,1,"reviewId").getBody();

        for (ReviewDTO review : reviews) {
            if(review.reviewId().equals(reviewId)){
                return review;
            }
        }
        throw new RuntimeException("cannot found review");
    }

    // 특정 리뷰 수정
    @PutMapping("/{reviewId}/block")
    @ResponseBody
    public ResponseEntity<Void> blockReview(@PathVariable Long reviewId, @RequestParam boolean block){
        return reviewAdapter.updateReviewBlock(reviewId,block);
    }
}
