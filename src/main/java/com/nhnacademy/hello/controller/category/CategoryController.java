package com.nhnacademy.hello.controller.category;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.image.ImageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories/books")
public class CategoryController {
    private final CategoryAdapter categoryAdapter;
    private final BookAdapter bookAdapter;
    private final ImageStore imageStore;

    private final int PAGE_SIZE = 18;

    @GetMapping
    public String bookList(
            // 검색 파라미터
            @RequestParam(value = "search", required = false) String search,

            // 페이징 파라미터 (size는 고정값 9)
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

            // 정렬 파라미터
            @RequestParam(value = "sort", required = false) String sort,

            // 추가적인 검색 및 정렬 파라미터
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "sortByLikeCount", required = false) Boolean sortByLikeCount,
            @RequestParam(value = "sortByLikeCount", required = false) Boolean sortByReviews,
            Model model
    ) {

        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        ResponseEntity<List<CategoryDTO>> categoryResponse = categoryAdapter.getCategories();

        List<CategoryDTO> categories;
        if (categoryResponse.getStatusCode().is2xxSuccessful()) {
            categories = categoryResponse.getBody();
            model.addAttribute("categories", categories);
        } else {
            // 에러 처리 로직 (예: 빈 리스트 또는 예외 던지기)
            model.addAttribute("categories", List.of());
        }

        List<Long> categoryIds = List.of(categoryId);

        Long totalBooks = bookAdapter.getTotalBooks(
                search,
                categoryIds,
                publisherName,
                authorName
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / PAGE_SIZE);

        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        List<String> sorting = new ArrayList<>();
        if (sort != null && !sort.isEmpty()) {
            sorting.add(sort);
        }
        sorting.add("bookId,desc");

        List<BookDTO> books = bookAdapter.getBooks(
                adjustedPage,
                PAGE_SIZE,
                sorting,
                search,
                categoryIds,
                publisherName,
                authorName,
                sortByLikeCount,
                sortByReviews
        );


        List<BookDTO> searchBooksWithImages = setImagePaths(books);
        model.addAttribute("searchBooksWithImages", searchBooksWithImages);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", PAGE_SIZE);

        return "book/categoryBooks";
    }

    /**
     * 도서 리스트에 이미지 경로를 설정하는 메소드
     */
    private List<BookDTO> setImagePaths(List<BookDTO> books) {
        return books.stream().map(book -> {
            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg"; // 기본 이미지 경로로 수정
            return new BookDTO(
                    book.bookId(),
                    book.bookTitle(),
                    book.bookDescription(),
                    book.bookPubDate(),
                    book.bookIsbn(),
                    book.bookOriginPrice(),
                    book.bookPrice(),
                    book.bookWrappable(),
                    book.bookView(),
                    book.bookAmount(),
                    book.bookSellCount(),
                    book.publisher(),
                    book.bookStatus(),
                    imagePath
            );
        }).collect(Collectors.toList());
    }

}
