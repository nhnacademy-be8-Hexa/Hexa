package com.nhnacademy.hello.common.controller.book;
import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;

import com.nhnacademy.hello.dto.review.ReviewDTO;
import com.nhnacademy.hello.dto.review.ReviewRequestDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.image.ImageStore;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = BookController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
public class BookControllerTest {

    @MockitoBean
    private BookAdapter bookAdapter;
    @MockitoBean
    private ReviewAdapter reviewAdapter;
    @MockitoBean
    private ImageStore imageStore;
    @MockitoBean
    private MemberReportAdapter memberReportAdapter;
    @MockitoBean
    private TagAdapter tagAdapter;
    @MockitoBean
    private BooKTagAdapter bookTagAdapter;
    @MockitoBean
    private PointDetailsAdapter pointDetailsAdapter;
    @MockitoBean
    private PointPolicyAdapter pointPolicyAdapter;
    @MockitoBean
    private LikeAdapter likeAdapter;
    @MockitoBean
    private MemberAdapter memberAdapter;
    @MockitoBean
    private CategoryAdapter categoryAdapter;
    // setImagePathsUtils 등 필요한 빈이 있다면 추가

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SetImagePathsUtils setImagePathsUtils;
    /**
     * GET /book/{bookId}
     * - 책 상세 페이지 요청 시 정상적으로 view와 모델을 반환하는지 테스트
     */
    @Test
    public void testBookDetail() throws Exception {
        Long bookId = 1L;
        // record를 이용한 dummy 데이터 생성
        BookDTO dummyBook = new BookDTO(
                bookId,
                "Dummy Title",
                "Dummy Description",
                LocalDate.now(),
                9781234567897L,
                20000,
                15000,
                true,
                100,
                50,
                25L,
                null, // PublisherRequestDTO (필요 시 더미객체 생성)
                null, // BookStatusRequestDTO
                "/images/dummy.jpg"
        );
        List<AuthorDTO> dummyAuthors = List.of(
                new AuthorDTO(1L, "Author1"),
                new AuthorDTO(2L, "Author2")
        );

        given(bookAdapter.getBook(bookId)).willReturn(dummyBook);
        given(bookAdapter.getAuthors(bookId)).willReturn(dummyAuthors);
        given(reviewAdapter.getTotalReviewsFromBook(bookId)).willReturn(ResponseEntity.ok(10L));
        given(reviewAdapter.getReviewsFromBook(bookId, 0, 10)).willReturn(ResponseEntity.ok(Collections.emptyList()));
        given(imageStore.getImage("bookThumbnail_" + bookId))
                .willReturn(Collections.singletonList("/images/dummy.jpg"));
        given(likeAdapter.hasLiked(eq(bookId), anyString()))
                .willReturn(ResponseEntity.ok(false));
        given(likeAdapter.getLikeCount(bookId)).willReturn(ResponseEntity.ok(5L));
        given(bookTagAdapter.getTagsByBook(bookId)).willReturn(ResponseEntity.ok(Collections.emptyList()));

        // reviewAdapter 관련 stub 추가
        given(reviewAdapter.checkOrderBook(anyString(), eq(bookId)))
                .willReturn(ResponseEntity.ok(false));
        given(reviewAdapter.checkReviews(anyString(), eq(bookId)))
                .willReturn(ResponseEntity.ok(false));

        // AuthInfoUtils.getUsername() 를 "testUser" 로 고정
        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(get("/book/{bookId}", bookId)
                            .param("size", "10")
                            .param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("book/bookDetail"))
                    .andExpect(model().attributeExists("book"))
                    .andExpect(model().attributeExists("authors"))
                    .andExpect(model().attributeExists("totalCount"))
                    .andExpect(model().attributeExists("reviews"));
        }
    }

    /**
     * POST /book/{bookId}/likes
     * - 인증되지 않은 사용자일 경우 /login으로 redirect 되는지 테스트
     */
    @Test
    public void testSubmitLikeWithoutUser() throws Exception {
        Long bookId = 1L;
        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn(null);

            mockMvc.perform(post("/book/{bookId}/likes", bookId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    /**
     * POST /book/{bookId}/likes
     * - 인증된 사용자인 경우 좋아요 토글 후 책 상세 페이지로 redirect 되는지 테스트
     */
    @Test
    public void testSubmitLikeWithUser() throws Exception {
        Long bookId = 1L;
        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(post("/book/{bookId}/likes", bookId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId));
        }
    }

    /**
     * POST /book/{bookId}/reviews
     * - 리뷰 작성 요청이 성공하는 경우 redirect 되는지 테스트
     */
    @Test
    public void testSubmitReviewSuccess() throws Exception {
        Long bookId = 1L;
        int size = 10;
        int page = 0;
        String reviewContent = "Great book!";
        BigDecimal rating = BigDecimal.valueOf(4.5);

        // 더미 BookDTO 생성 (모델 재설정 및 검증 오류 경로에서도 사용됨)
        BookDTO dummyBook = new BookDTO(
                bookId,
                "Dummy Title",
                "Dummy Description",
                LocalDate.now(),
                9781234567897L,
                20000,
                15000,
                true,
                100,
                50,
                25L,
                null,   // PublisherRequestDTO (필요 시 더미 객체 생성)
                null,   // BookStatusRequestDTO
                "/images/dummy.jpg"
        );

        // 각종 의존 메서드 Stub 설정 (재설정 로직과 성공 분기 모두 대응)
        given(bookAdapter.getBook(bookId)).willReturn(dummyBook);
        given(bookAdapter.getAuthors(bookId))
                .willReturn(List.of(new AuthorDTO(1L, "Author1"), new AuthorDTO(2L, "Author2")));
        given(bookAdapter.getLikeCount(bookId))
                .willReturn(ResponseEntity.ok(5L));
        given(reviewAdapter.getReviewsFromBook(bookId, page, size))
                .willReturn(ResponseEntity.ok(Collections.emptyList()));
        given(reviewAdapter.getTotalReviewsFromBook(bookId))
                .willReturn(ResponseEntity.ok(10L));
        given(imageStore.getImage("bookThumbnail_" + bookId))
                .willReturn(Collections.singletonList("/images/dummy.jpg"));
        given(reviewAdapter.createReview(any(ReviewRequestDTO.class), eq("testUser"), eq(bookId)))
                .willReturn(ResponseEntity.ok().build());

        // AuthInfoUtils.getUsername()를 "testUser"로 고정하여 테스트 환경 구축
        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(post("/book/{bookId}/reviews", bookId)
                            // ReviewRequestDTO의 필드명이 reviewContent와 reviewRating라고 가정
                            .param("reviewContent", reviewContent)
                            .param("reviewRating", rating.toString())
                            .param("imagesIncluded", "false")
                            .param("size", String.valueOf(size))
                            .param("page", String.valueOf(page))
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId + "?success=true&page=" + page + "&size=" + size));
        }
    }

    /**
     * POST /book/{bookId}/reviews/{reviewId}
     * - 리뷰 수정 요청 성공 후 redirect 되는지 테스트
     */
    @Test
    public void testBookReviewUpdateSuccess() throws Exception {
        Long bookId = 1L;
        Long reviewId = 100L;
        String reviewContent = "Updated review";
        BigDecimal rating = BigDecimal.valueOf(5.0);
        int size = 10;
        int page = 1;

        // 리뷰 업데이트 성공 시 2xx response 반환
        given(reviewAdapter.updateReview(any(ReviewRequestDTO.class), eq(reviewId)))
                .willReturn(ResponseEntity.ok().build());

        // (필요하다면 업데이트 성공 분기에서는 추가 stub을 설정할 수 있으나,
        // 정상적인 binding이 이루어지면 검증 실패 분기는 실행되지 않습니다.)

        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(post("/book/{bookId}/reviews/{reviewId}", bookId, reviewId)
                            // DTO의 필드명이 reviewContent와 reviewRating라면 아래와 같이 전달해야 합니다.
                            .param("reviewContent", reviewContent)
                            .param("reviewRating", rating.toString())
                            .param("size", String.valueOf(size))
                            .param("page", String.valueOf(page))
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId + "?success=true&page=" + page + "&size=" + size));
        }
    }

    /**
     * POST /book/{bookId}/reviews/{reviewId}/report
     * - 리뷰 신고 요청이 성공(CREATED 응답)하면 redirect 되는지 테스트
     */
    @Test
    public void testReviewReportSuccess() throws Exception {
        Long bookId = 1L;
        Long reviewId = 100L;

        given(memberReportAdapter.saveMemberReport(eq("testUser"), eq(reviewId)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(post("/book/{bookId}/reviews/{reviewId}/report", bookId, reviewId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId));
        }
    }

    /**
     * POST /book/{bookId}/reviews/{reviewId}/report
     * - 리뷰 신고 요청이 실패(이미 신고 등)하면 redirect 되는지 테스트
     */
    @Test
    public void testReviewReportFailure() throws Exception {
        Long bookId = 1L;
        Long reviewId = 100L;

        // 예를 들어 4xx 에러를 반환하는 경우
        given(memberReportAdapter.saveMemberReport(eq("testUser"), eq(reviewId)))
                .willReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        try (MockedStatic<AuthInfoUtils> authUtil = Mockito.mockStatic(AuthInfoUtils.class)) {
            authUtil.when(AuthInfoUtils::getUsername).thenReturn("testUser");

            mockMvc.perform(post("/book/{bookId}/reviews/{reviewId}/report", bookId, reviewId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId));
        }
    }
}
