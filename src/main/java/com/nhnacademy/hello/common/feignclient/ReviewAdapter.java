package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.review.ReviewDTO;
import com.nhnacademy.hello.dto.review.ReviewRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "reviewAdapter")
public interface ReviewAdapter {

    @PostMapping("/members/{memberId}/books/{bookId}/reviews")
    public ResponseEntity<Void> createReview(
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @PathVariable String memberId,
            @PathVariable Long bookId);

    @GetMapping("/members/{memberId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromMember(
            @PathVariable String memberId,
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    // 특정 회원의 리뷰 총계를 조회
    @GetMapping("/members/{memberId}/reviews/total")
    public ResponseEntity<Long> getTotalReviews(@PathVariable String memberId);


    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @PathVariable Long reviewId);

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromBook(
            @PathVariable Long bookId,
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
            );

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId);

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromAdmin(
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    @PutMapping("/reviews/{reviewId}/block")
    public ResponseEntity<Void> updateReviewBlock(
            @PathVariable Long reviewId,
            @RequestParam boolean block
    );

    // 신고 5회 이상 받은 리뷰 목록 조회
    @GetMapping("/reviews/highReport")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromHighReport(
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    // 신고 5회 이상의 리뷰 목록 조회 페이징을 위한 총계
    @GetMapping("/reviews/highReport/total")
    public ResponseEntity<Long> getReviewsFromHighReportTotal();
}
