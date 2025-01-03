package com.nhnacademy.hello.controller.index;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.image.ImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final BookAdapter bookAdapter;
    private final CategoryAdapter categoryAdapter;
    private final ImageStore imageStore;

    @GetMapping(value = {"/index.html", "/"})
    public String index(Model model) {

        // 베스트셀러
        List<BookDTO> best_seller_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, null, true, null, null
        );
        List<BookDTO> bestSellerWithImages = setImagePaths(best_seller_books);
        model.addAttribute("best_seller_books", bestSellerWithImages);

        // 가장 많이 클릭된 책 (조회수)
        List<BookDTO> most_viewed_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, true, null, null, null
        );
        List<BookDTO> mostViewedWithImages = setImagePaths(most_viewed_books);
        model.addAttribute("most_viewed_books", mostViewedWithImages);

        // 좋아요가 가장 많은 책
        List<BookDTO> most_liked_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, null, null, true, null
        );
        List<BookDTO> mostLikedWithImages = setImagePaths(most_liked_books);
        model.addAttribute("most_liked_books", mostLikedWithImages);

        // 카테고리 목록 조회
        ResponseEntity<List<CategoryDTO>> categoryResponse = categoryAdapter.getCategories();
        if (categoryResponse.getStatusCode().is2xxSuccessful()) {
            List<CategoryDTO> categories = categoryResponse.getBody();
            model.addAttribute("categories", categories);
        } else {
            // 에러 처리 로직 (예: 빈 리스트 또는 예외 던지기)
            model.addAttribute("categories", List.of());
        }

        return "index/index";
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
                    imagePath,
                    book.bookLikeCount()
            );
        }).collect(Collectors.toList());
    }
}
