package com.nhnacademy.hello.controller.book;


import com.nhnacademy.hello.common.feignclient.ElasticSearchAdapter;
import com.nhnacademy.hello.dto.book.BookSearchDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class ElasticSearchController {
    private final ElasticSearchAdapter elasticSearchAdapter;

    @GetMapping
    public String searchPage(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                             @RequestParam String search,
                             Model model) {
        final int size = 18;

        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;
        Long totalBooks = elasticSearchAdapter.getTotalSearchBooks(search).getBody();
        int totalPages = (int) Math.ceil((double) totalBooks / size);

        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        List<BookSearchDTO> searchBooks = elasticSearchAdapter.searchBooks(adjustedPage, size, search);
        
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("searchBooks", searchBooks);

        return "book/bookSearch";
    }
}
