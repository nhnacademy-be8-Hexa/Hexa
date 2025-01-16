package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.review.ReviewDTO;
import com.nhnacademy.hello.dto.review.ReviewRequestDTO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hexa-gateway", contextId = "reviewAdapter")
public interface ReviewAdapter {

    @PostMapping("/api/members/{memberId}/books/{bookId}/reviews")
    public ResponseEntity<Void> createReview(
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @PathVariable String memberId,
            @PathVariable Long bookId);

    @GetMapping("/api/members/{memberId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromMember(
            @PathVariable String memberId,
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );


    // 특정 주문에 특정 멤버가 주문한 도서가 있는지 확인
    @GetMapping("/api/members/{memberId}/orders/books/{bookId}")
    public ResponseEntity<Boolean> checkOrderBook(
            @PathVariable String memberId,
            @PathVariable Long bookId
    );

    // 특정 회원의 리뷰 총계를 조회
    @GetMapping("/api/members/{memberId}/reviews/total")
    public ResponseEntity<Long> getTotalReviews(@PathVariable String memberId);


    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @PathVariable Long reviewId);

    @GetMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromBook(
            @PathVariable Long bookId,
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId);

    @GetMapping("/api/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromAdmin(
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    @PutMapping("/api/reviews/{reviewId}/block")
    public ResponseEntity<Void> updateReviewBlock(
            @PathVariable Long reviewId,
            @RequestParam boolean block
    );

    // 신고 5회 이상 받은 리뷰 목록 조회
    @GetMapping("/api/reviews/highReport")
    public ResponseEntity<List<ReviewDTO>> getReviewsFromHighReport(
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    // 신고 5회 이상의 리뷰 목록 조회 페이징을 위한 총계
    @GetMapping("/api/reviews/highReport/total")
    public ResponseEntity<Long> getReviewsFromHighReportTotal();

    @GetMapping("/api/books/{bookId}/reviews/total")
    public ResponseEntity<Long> getTotalReviewsFromBook(
            @PathVariable Long bookId
    );

    @GetMapping("/api/members/{memberId}/books/{bookId}/reviews")
    public ResponseEntity<Boolean> checkReviews(
            @PathVariable String memberId,
            @PathVariable Long bookId
    );

    @GetMapping("/api/books/{bookId}/review-rating")
    ResponseEntity<BigDecimal> getReviewRating(@PathVariable Long bookId);

    @GetMapping("/api/reviews/{reviewId}")
    ResponseEntity<ReviewDTO> getReview(
            @PathVariable Long reviewId
    );
}
