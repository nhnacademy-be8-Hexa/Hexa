package com.nhnacademy.hello.common.controller.category;


import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.controller.category.CategoryController;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.image.ImageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryAdapter categoryAdapter;

    @MockitoBean
    private BookAdapter bookAdapter;

    @MockitoBean
    private ImageStore imageStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("GET /categories/books - 카테고리별 도서 리스트")
    void testBookList() throws Exception {
        // given
        List<CategoryDTO> mockCategories = new ArrayList<>();

        CategoryDTO category1 = new CategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Fiction");

        CategoryDTO category2 = new CategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Non-Fiction");

        mockCategories.add(category1);
        mockCategories.add(category2);
        when(categoryAdapter.getCategories())
                .thenReturn(ResponseEntity.ok(mockCategories));

        Long totalBooks = 30L;
        when(bookAdapter.getTotalBooks(anyString(), anyList(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(totalBooks));

        List<BookDTO> mockBooks = List.of(
                new BookDTO(1L, "Book 1", "Description 1", null, 123456789L, 20000, 18000, true, 100, 50, 10L, null, null, null),
                new BookDTO(2L, "Book 2", "Description 2", null, 987654321L, 25000, 22000, true, 120, 60, 15L, null, null, null)
        );
        // 수정: getBooks의 stub을 새 시그니처에 맞게 9개 인자로 설정
        when(bookAdapter.getBooks(
                anyInt(),          // page
                anyInt(),          // size
                anyList(),         // sort 리스트 (예: [sort, "bookId,desc"] 등)
                anyString(),       // search
                anyList(),         // categoryIds
                anyString(),       // publisherName
                anyString(),       // authorName
                any(),             // sortByLikeCount (Boolean)
                any()              // sortByReviews (Boolean)
        )).thenReturn(mockBooks);

        // 이미지 경로 관련 stub: imageStore.getImage() 호출 시 이미지 경로 목록 반환
        when(imageStore.getImage(anyString())).thenReturn(List.of("/images/mock-image.jpg"));

        // when & then
        mockMvc.perform(get("/categories/books")
                        .param("search", "book")
                        .param("page", "1")
                        .param("sort", "title")
                        .param("categoryId", "1")   // 단일 categoryId --> 내부에서 List<Long>로 변환될 것으로 가정
                        .param("publisherName", "Publisher")
                        .param("authorName", "Author")
                        // 원래 테스트의 파라미터들은 컨트롤러에서 사용 후 내부적으로 변환됨
                        .param("sortByView", "true")
                        .param("sortBySellCount", "false")
                        .param("sortByLikeCount", "false")
                        .param("latest", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/categoryBooks"))
                .andExpect(model().attributeExists("searchBooksWithImages", "categories", "currentPage", "totalPages", "size"))
                // currentPage: 1, totalPages: ceil(30 / size) -> size가 18 (고정값) 인 경우 총 2페이지 가정.
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(model().attribute("size", 18));

        verify(categoryAdapter, times(1)).getCategories();
        verify(bookAdapter, times(1)).getTotalBooks(anyString(), anyList(), anyString(), anyString());
        verify(bookAdapter, times(1)).getBooks(anyInt(), anyInt(), anyList(), anyString(), anyList(), anyString(), anyString(), any(), any());
    }

}
