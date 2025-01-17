package com.nhnacademy.hello.controller.book;


import com.nhnacademy.hello.common.feignclient.ElasticSearchAdapter;
import com.nhnacademy.hello.dto.book.SearchBookDTO;
import com.nhnacademy.hello.image.ImageStore;
import java.util.List;
import java.util.stream.Collectors;
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
    private final ImageStore imageStore;

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

        List<SearchBookDTO> searchBooks = elasticSearchAdapter.searchBooks(adjustedPage, size, search);
        List<SearchBookDTO> searchBooksWithImages = setImagePaths(searchBooks);
        model.addAttribute("searchBooksWithImages", searchBooksWithImages);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("totalBooks", totalBooks);

        return "book/bookSearch";
    }

    /**
     * 도서 리스트에 이미지 경로를 설정하는 메소드
     */
    private List<SearchBookDTO> setImagePaths(List<SearchBookDTO> searchBooks) {
        return searchBooks.stream().map(book -> {
            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg"; // 기본 이미지 경로로 수정
            return new SearchBookDTO(
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
