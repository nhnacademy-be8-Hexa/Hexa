package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberReportAdapter;
import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.dto.review.ReviewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewManageControllerTest {

    @InjectMocks
    private ReviewManageController reviewManageController;

    @Mock
    private ReviewAdapter reviewAdapter;

    @Mock
    private MemberReportAdapter memberReportAdapter;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("신고 5회 이상 받은 리뷰 목록 조회 성공")
    void getReportedReviews_Success() {
        // Given
        int page = 1;
        int size = 9;
        String sort = "reportCount";
        List<ReviewDTO> mockReviews = List.of(
                new ReviewDTO(1L, "리뷰 내용 1", BigDecimal.valueOf(4.5),
                        new ReviewDTO.MemberDTO("member1"), false),
                new ReviewDTO(2L, "리뷰 내용 2", BigDecimal.valueOf(3.0),
                        new ReviewDTO.MemberDTO("member2"), true)
        );

        when(reviewAdapter.getReviewsFromHighReport(eq(0), eq(size), eq(sort)))
                .thenReturn(ResponseEntity.ok(mockReviews));
        when(reviewAdapter.getReviewsFromHighReportTotal())
                .thenReturn(ResponseEntity.ok(15L));

        // When
        String viewName = reviewManageController.getReportedReviews(page, size, sort, null, model);

        // Then
        assertEquals("admin/reviewManage", viewName);
        verify(model).addAttribute("reviews", mockReviews);
        verify(model).addAttribute("currentPage", page);
        verify(model).addAttribute("totalPage", 2); // 15 / 9 = 2 pages
        verify(model).addAttribute("size", size);
        verify(model).addAttribute("sort", sort);
        verify(model).addAttribute("search", null);
    }

    @Test
    @DisplayName("리뷰 목록 조회 실패 시 에러 처리")
    void getReportedReviews_Failure() {
        // Given
        when(reviewAdapter.getReviewsFromHighReport(anyInt(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("데이터 가져오기 실패"));

        // When
        String viewName = reviewManageController.getReportedReviews(1, 9, null, null, model);

        // Then
        assertEquals("error/500", viewName);
        verify(model).addAttribute("errorMessage", "리뷰 데이터를 가져오는 중 문제가 발생했습니다.");
    }

    @Test
    @DisplayName("특정 리뷰 상세 조회 성공")
    void getReviewDetail_Success() {
        // Given
        Long reviewId = 1L;

        // getReviewsFromAdmin은 빈 목록을 반환하도록 설정 (이미 있음)
        when(reviewAdapter.getReviewsFromAdmin(anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of()));

        when(reviewAdapter.getReview(anyLong()))
                .thenReturn(ResponseEntity.ok(null));

        // When
        String viewName = reviewManageController.getReviewDetail(reviewId, model);

        // Then: 리뷰 상세 조회 실패 시, 리뷰 목록 페이지로 리다이렉트
        assertEquals("admin/reviewDetail", viewName);
    }

    @Test
    @DisplayName("특정 리뷰 블락 성공")
    void blockReview_Success() {
        // Given
        Long reviewId = 1L;
        boolean block = true;

        when(memberReportAdapter.allDelete(eq(reviewId))).thenReturn(ResponseEntity.ok().build());
        when(reviewAdapter.updateReviewBlock(eq(reviewId), eq(block))).thenReturn(ResponseEntity.ok().build());

        // When
        ResponseEntity<Void> response = reviewManageController.blockReview(reviewId, block);

        // Then
        assertEquals(ResponseEntity.ok().build(), response);
        verify(memberReportAdapter).allDelete(reviewId);
        verify(reviewAdapter).updateReviewBlock(reviewId, block);
    }
}