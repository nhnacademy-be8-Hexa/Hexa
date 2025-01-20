package com.nhnacademy.hello.common.controller.book;


import com.nhnacademy.hello.common.feignclient.ElasticSearchAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.controller.book.ElasticSearchController;
import com.nhnacademy.hello.dto.book.BookStatusRequestDTO;
import com.nhnacademy.hello.dto.book.PublisherRequestDTO;
import com.nhnacademy.hello.dto.book.SearchBookDTO;
import com.nhnacademy.hello.image.ImageStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ElasticSearchController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class ElasticSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticSearchAdapter elasticSearchAdapter;

    @MockBean
    private ImageStore imageStore;

    @Test
    @DisplayName("GET /search - Search Books")
    void testSearchPage() throws Exception {

        PublisherRequestDTO publisher = new PublisherRequestDTO(1L,"하이");
        BookStatusRequestDTO bookStatus = new BookStatusRequestDTO(2L,"dd");
        // Mock data
        List<SearchBookDTO> mockBooks = List.of(
                new SearchBookDTO(1L, "Book Title", "Description", "2025-01-01", 1234567890123L, 20000, 18000, true, 100, 50, 25L,  publisher, bookStatus, "/images/mock-image.jpg")
        );




        Mockito.when(elasticSearchAdapter.getTotalSearchBooks(anyString()))
                .thenReturn(ResponseEntity.ok(20L)); // Mock total books

        Mockito.when(elasticSearchAdapter.searchBooks(anyInt(), anyInt(), anyString()))
                .thenReturn(mockBooks); // Mock search results

        Mockito.when(imageStore.getImage(anyString()))
                .thenReturn(List.of("/images/mock-image.jpg")); // Mock image paths

        // Perform request and validate response
        mockMvc.perform(get("/search")
                        .param("page", "1")
                        .param("search", "keyword"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/bookSearch"))
                .andExpect(model().attributeExists("searchBooksWithImages", "search", "currentPage", "totalPages", "size"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2));

        // Verify interactions
        Mockito.verify(elasticSearchAdapter, Mockito.times(1)).getTotalSearchBooks("keyword");
        Mockito.verify(elasticSearchAdapter, Mockito.times(1)).searchBooks(0, 18, "keyword");
        Mockito.verify(imageStore, Mockito.times(1)).getImage("bookThumbnail_1");
    }
}
