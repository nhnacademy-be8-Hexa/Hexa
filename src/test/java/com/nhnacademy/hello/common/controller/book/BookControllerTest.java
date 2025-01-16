package com.nhnacademy.hello.common.controller.book;
import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;

import com.nhnacademy.hello.dto.review.ReviewDTO;
import com.nhnacademy.hello.dto.review.ReviewRequestDTO;
import com.nhnacademy.hello.image.ImageStore;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookAdapter bookAdapter;

    @MockBean
    private ReviewAdapter reviewAdapter;

    @MockBean
    private ImageStore imageStore;

    @MockBean
    private MemberReportAdapter memberReportAdapter;

    @MockBean
    private SetImagePathsUtils setImagePathsUtils;

    @MockBean
    private TagAdapter tagAdapter;

    @MockBean
    private BooKTagAdapter bookTagAdapter;

    @MockBean
    private PointDetailsAdapter pointDetailsAdapter;

    @MockBean
    private PointPolicyAdapter pointPolicyAdapter;

    @MockBean
    private LikeAdapter likeAdapter;

    @MockitoBean
    private MemberAdapter memberAdapter;

    @MockitoBean
    private CategoryAdapter categoryAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("GET /book/{bookId} - 책 상세 정보 가져오기")
    void testBookDetail() throws Exception {
        // given
        Long bookId = 1L;
        BookDTO mockBook = mock(BookDTO.class);
        List<AuthorDTO> authors = List.of(mock(AuthorDTO.class));
        List<ReviewDTO> reviews = List.of(mock(ReviewDTO.class));

        when(bookAdapter.getBook(bookId)).thenReturn(mockBook);
        when(bookAdapter.getAuthors(bookId)).thenReturn(authors);
        when(reviewAdapter.getTotalReviewsFromBook(bookId)).thenReturn(ResponseEntity.ok(10L));
        when(reviewAdapter.getReviewsFromBook(eq(bookId), anyInt(), anyInt())).thenReturn(ResponseEntity.ok(reviews));
        when(imageStore.getImage(anyString())).thenReturn(List.of("/images/mock-image.jpg"));
        when(AuthInfoUtils.getUsername()).thenReturn("mockUser");
        when(likeAdapter.hasLiked(eq(bookId), anyString())).thenReturn(ResponseEntity.ok(true));
        when(likeAdapter.getLikeCount(bookId)).thenReturn(ResponseEntity.ok(5L));

        // when & then
        mockMvc.perform(get("/book/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookDetail"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("reviews", reviews))
                .andExpect(model().attribute("thumbnailImage", "/images/mock-image.jpg"))
                .andExpect(model().attribute("likeCount", 5L));

        verify(bookAdapter, times(1)).getBook(bookId);
        verify(reviewAdapter, times(1)).getReviewsFromBook(eq(bookId), anyInt(), anyInt());
    }

    @Test
    @DisplayName("POST /book/{bookId}/likes - 좋아요 토글")
    void testSubmitLike() throws Exception {
        // given
        Long bookId = 1L;
        when(AuthInfoUtils.getUsername()).thenReturn("mockUser");

        // when & then
        mockMvc.perform(post("/book/{bookId}/likes", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        verify(likeAdapter, times(1)).toggleLike(bookId, "mockUser");
    }

    @Test
    @DisplayName("POST /book/{bookId}/reviews - 리뷰 작성")
    void testSubmitReview() throws Exception {
        // given
        Long bookId = 1L;

        // Mock ReviewAdapter
        ReviewAdapter mockAdapter = mock(ReviewAdapter.class);

        // DTO 생성
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO("Good book", BigDecimal.valueOf(4.5));

        // Static method mocking
        try (MockedStatic<AuthInfoUtils> mockedStatic = mockStatic(AuthInfoUtils.class)) {
            mockedStatic.when(AuthInfoUtils::getUsername).thenReturn("mockUser");

            // Mock behavior
            when(mockAdapter.createReview(eq(reviewRequestDTO), eq("mockUser"), eq(bookId)))
                    .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

            // when & then
            mockMvc.perform(post("/book/{bookId}/reviews", bookId)
                            .flashAttr("reviewRequestDTO", reviewRequestDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/book/" + bookId + "?success=true&page=0&size=10"));

            // Verify behavior
            verify(mockAdapter, times(1)).createReview(eq(reviewRequestDTO), eq("mockUser"), eq(bookId));
        }
    }


    @Test
    @DisplayName("POST /book/{bookId}/reviews/{reviewId}/report - 리뷰 신고")
    void testReviewReport() throws Exception {
        // given
        Long bookId = 1L;
        Long reviewId = 1L;
        when(AuthInfoUtils.getUsername()).thenReturn("mockUser");
        when(memberReportAdapter.saveMemberReport("mockUser", reviewId))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        // when & then
        mockMvc.perform(post("/book/{bookId}/reviews/{reviewId}/report", bookId, reviewId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        verify(memberReportAdapter, times(1)).saveMemberReport("mockUser", reviewId);
    }
}
