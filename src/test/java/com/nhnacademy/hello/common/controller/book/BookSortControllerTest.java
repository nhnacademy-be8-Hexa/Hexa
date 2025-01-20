package com.nhnacademy.hello.common.controller.book;


import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookSortController;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookSortController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class BookSortControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookAdapter bookAdapter;

    @MockitoBean
    private SetImagePathsUtils setImagePathsUtils;

    @MockitoBean
    private TagAdapter tagAdapter;

    @MockitoBean
    private BooKTagAdapter booKTagAdapter;

    @Test
    @DisplayName("GET /bestsellers - Bestsellers Page")
    void testBestsellers() throws Exception {
        List<BookDTO> mockBooks = List.of(Mockito.mock(BookDTO.class));

        // 새로운 getBooks는 9개의 파라미터를 받습니다.
        Mockito.when(bookAdapter.getBooks(
                anyInt(),          // page
                anyInt(),          // size
                anyList(),         // sort (List<String>)
                anyString(),       // search
                anyList(),         // categoryIds
                anyString(),       // publisherName
                anyString(),       // authorName
                any(),             // sortByLikeCount
                any()              // sortByReviews
        )).thenReturn(mockBooks);

        Mockito.when(bookAdapter.getTotalBooks(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(20L));

        Mockito.when(setImagePathsUtils.setImagePaths(mockBooks)).thenReturn(mockBooks);

        mockMvc.perform(get("/bestsellers"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSort"))
                .andExpect(model().attributeExists("searchBooksWithImages", "currentPage", "totalPages", "size"));
    }


    @Test
    @DisplayName("GET /newarrivals - New Arrivals Page")
    void testNewArrivals() throws Exception {
        List<BookDTO> mockBooks = List.of(Mockito.mock(BookDTO.class));

        Mockito.when(bookAdapter.getBooks(
                anyInt(),
                anyInt(),
                anyList(),
                anyString(),
                anyList(),
                anyString(),
                anyString(),
                any(),
                any()
        )).thenReturn(mockBooks);

        Mockito.when(bookAdapter.getTotalBooks(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(20L));

        Mockito.when(setImagePathsUtils.setImagePaths(mockBooks)).thenReturn(mockBooks);

        mockMvc.perform(get("/newarrivals"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSort"))
                .andExpect(model().attributeExists("searchBooksWithImages", "currentPage", "totalPages", "size"));
    }


    @Test
    @DisplayName("GET /manyreview - Many Reviews Page")
    void testManyReviews() throws Exception {
        List<BookDTO> mockBooks = List.of(Mockito.mock(BookDTO.class));

        Mockito.when(bookAdapter.getBooks(
                anyInt(),
                anyInt(),
                anyList(),
                anyString(),
                anyList(),
                anyString(),
                anyString(),
                any(),
                any()
        )).thenReturn(mockBooks);

        Mockito.when(bookAdapter.getTotalBooks(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(20L));

        Mockito.when(setImagePathsUtils.setImagePaths(mockBooks)).thenReturn(mockBooks);

        mockMvc.perform(get("/manyreview"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSort"))
                .andExpect(model().attributeExists("searchBooksWithImages", "currentPage", "totalPages", "size"));
    }


    @Test
    @DisplayName("GET /name - Sort by Name Page")
    void testSortByName() throws Exception {
        List<BookDTO> mockBooks = List.of(Mockito.mock(BookDTO.class));

        Mockito.when(bookAdapter.getBooks(
                anyInt(),
                anyInt(),
                anyList(),
                anyString(),
                anyList(),
                anyString(),
                anyString(),
                any(),
                any()
        )).thenReturn(mockBooks);

        // 만약 getTotalBooks 메서드의 파라미터가 다르다면 그대로 사용
        Mockito.when(bookAdapter.getTotalBooks(anyString(), anyList(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(20L));

        Mockito.when(setImagePathsUtils.setImagePaths(mockBooks)).thenReturn(mockBooks);

        mockMvc.perform(get("/name").param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSort"))
                .andExpect(model().attributeExists("searchBooksWithImages", "currentPage", "totalPages", "size"));
    }


    @Test
    @DisplayName("GET /tag/{tagId} - Books by Tag Page")
    void testBooksByTag() throws Exception {
        Long tagId = 1L;
        List<BookDTO> mockBooks = List.of(Mockito.mock(BookDTO.class));
        TagDTO mockTag = Mockito.mock(TagDTO.class);
        Mockito.when(mockTag.getTagName()).thenReturn("Fiction");
        Mockito.when(booKTagAdapter.getBooksByTag(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.ok(mockBooks));
        Mockito.when(booKTagAdapter.getBookCountByTag(anyLong()))
                .thenReturn(ResponseEntity.ok(20));
        Mockito.when(setImagePathsUtils.setImagePaths(mockBooks)).thenReturn(mockBooks);
        Mockito.when(tagAdapter.getTagById(anyLong())).thenReturn(ResponseEntity.ok(mockTag));

        mockMvc.perform(get("/tag/{tagId}", tagId))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSort"))
                .andExpect(model().attributeExists("searchBooksWithImages", "currentPage", "totalPages", "size", "tagName"));
    }
}
