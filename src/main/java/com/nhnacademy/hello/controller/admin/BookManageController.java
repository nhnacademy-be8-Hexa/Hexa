package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.BookStatusAdapter;
import com.nhnacademy.hello.common.feignclient.PublisherAdapter;
import com.nhnacademy.hello.dto.book.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/bookManage")
public class BookManageController {
    private final BookAdapter bookAdapter;
    private final PublisherAdapter publisherAdapter;
    private final BookStatusAdapter bookStatusAdapter;

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

        // 도서 총계 가져오기 (필터링 조건을 반영)
        Long totalBooks = bookAdapter.getTotalBooks(
                search,
                categoryIds,
                publisherName,
                authorName
        ).getBody();

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

    // 도서 등록 폼
    @GetMapping("/new")
    public String showCreateBookForm(Model model) {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
                "", "", null, null, 0, 0, false, "", ""
        );
        model.addAttribute("bookRequestDTO", bookRequestDTO);
        model.addAttribute("isEdit", false);

        // 출판사 목록 추가 (선택 사항)
        List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
        model.addAttribute("publishers", publishers);

        return "admin/bookCreateForm"; // 도서 등록 폼 템플릿
    }

    // 도서 등록 처리
    @PostMapping("/new")
    public String createBook(@Valid @ModelAttribute("bookRequestDTO") BookRequestDTO bookRequestDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
            model.addAttribute("publishers", publishers);
            return "admin/bookCreateForm";
        }

        // 도서 생성 로직
        bookAdapter.createBook(bookRequestDTO);
        return "redirect:/admin/bookManage/books";
    }

    // 도서 수정 폼
    @GetMapping("/edit/{bookId}")
    public String showEditBookForm(@PathVariable Long bookId, Model model) {
        BookDTO book = bookAdapter.getBook(bookId);
        if (book == null) {
            // 도서가 존재하지 않을 경우 처리 (예: 에러 페이지로 이동)
            return "redirect:/admin/bookManage/books";
        }

        // BookDTO를 BookUpdateRequestDTO로 매핑
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                book.bookTitle(),
                book.bookDescription(),
                book.bookPrice(),
                book.bookWrappable(),
                book.bookStatus().bookStatusId().toString()
        );
        List<BookStatusRequestDTO> bookStatuses = bookStatusAdapter.getBookStatus(bookId.toString());

        model.addAttribute("bookUpdateRequestDTO", bookUpdateRequestDTO);
        model.addAttribute("isEdit", true);
        model.addAttribute("bookId", bookId);
        model.addAttribute("bookStatuses", bookStatuses);

        // 출판사 목록 추가 (선택 사항)
        List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
        model.addAttribute("publishers", publishers);

        return "admin/bookEditForm"; // 도서 수정 폼 템플릿
    }

    @PostMapping("/edit/{bookId}")
    public String updateBook(@PathVariable Long bookId,
                             @Valid @ModelAttribute("bookUpdateRequestDTO") BookUpdateRequestDTO bookUpdateRequestDTO,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("bookId", bookId);

            List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
            model.addAttribute("publishers", publishers);

            List<BookStatusRequestDTO> bookStatuses = bookStatusAdapter.getBookStatus(bookId.toString());
            model.addAttribute("bookStatuses", bookStatuses);

            return "admin/bookEditForm";
        }

        bookAdapter.updateBook(bookId, bookUpdateRequestDTO);
        return "redirect:/admin/bookManage";
    }
}
