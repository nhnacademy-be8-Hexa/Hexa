package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/bookManage")
public class BookManageController {
    private final BookAdapter bookAdapter;

    @GetMapping
    public String bookList(
            // 검색 파라미터
            @RequestParam(value = "search", required = false) String search,

            // 페이징 파라미터 (size는 고정값 9)
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

            // 정렬 파라미터
            @RequestParam(value = "sort", required = false, defaultValue = "title") String sort,

            // 추가적인 검색 및 정렬 파라미터
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "sortByView", required = false, defaultValue = "false") Boolean sortByView,
            @RequestParam(value = "sortBySellCount", required = false, defaultValue = "false") Boolean sortBySellCount,
            @RequestParam(value = "sortByLikeCount", required = false, defaultValue = "false") Boolean sortByLikeCount,
            @RequestParam(value = "latest", required = false, defaultValue = "false") Boolean latest,

            Model model
    ) {
        // 고정된 페이지 사이즈
        final int size = 9;

        // 페이지 번호 조정 (1 기반을 0 기반으로 변환)
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        // 도서 총계 가져오기
        Long totalBooks = bookAdapter.getTotalBooks().getBody();

        // 전체 페이지 수 계산 (size가 9인 것을 전제로 함)
        int totalPages = (int) Math.ceil((double) totalBooks / size);

        // 현재 페이지가 총 페이지 수를 초과하지 않도록 조정
        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        // 도서 목록 가져오기
        List<BookDTO> books = bookAdapter.getBooks(
                adjustedPage,
                size,
                sort,
                search,
                categoryIds,
                publisherName,
                authorName,
                sortByView,
                sortBySellCount,
                sortByLikeCount,
                latest
        );

        // 모델에 데이터 추가
        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("search", search);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("publisherName", publisherName);
        model.addAttribute("authorName", authorName);
        model.addAttribute("sortByView", sortByView);
        model.addAttribute("sortBySellCount", sortBySellCount);
        model.addAttribute("sortByLikeCount", sortByLikeCount);
        model.addAttribute("latest", latest);

        // 뷰 이름 반환 (예: "admin/bookManage")
        return "admin/bookManage";
    }
}
