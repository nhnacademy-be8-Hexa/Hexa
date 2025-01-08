package com.nhnacademy.hello.controller.category;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.image.ImageStore;
import java.util.ArrayList;
import java.util.Collections;
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


    @GetMapping
    public String bookList(
            // 검색 파라미터
            @RequestParam(value = "search", required = false) String search,

            // 페이징 파라미터 (size는 고정값 9)
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

            // 정렬 파라미터
            @RequestParam(value = "sort", required = false, defaultValue = "title") String sort,

            // 추가적인 검색 및 정렬 파라미터
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "sortByView", required = false, defaultValue = "false") Boolean sortByView,
            @RequestParam(value = "sortBySellCount", required = false, defaultValue = "false") Boolean sortBySellCount,
            @RequestParam(value = "sortByLikeCount", required = false, defaultValue = "false") Boolean sortByLikeCount,
            @RequestParam(value = "latest", required = false, defaultValue = "false") Boolean latest,
            Model model
    ) {

        final int size = 18;

        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;


        ResponseEntity<List<CategoryDTO>> categoryResponse = categoryAdapter.getCategories();

        List<CategoryDTO> categories;
        if (categoryResponse.getStatusCode().is2xxSuccessful()) {
            categories = categoryResponse.getBody();
            model.addAttribute("categories", categories);
        } else {
            // 에러 처리 로직 (예: 빈 리스트 또는 예외 던지기)
            categories = List.of();
            model.addAttribute("categories", List.of());
        }

        List<Long> categoryIds =
                extractCategoryIds(categories != null ? categories : Collections.emptyList(), categoryId);

        Long totalBooks = bookAdapter.getTotalBooks(
                search,
                categoryIds,
                publisherName,
                authorName
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / size);

        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

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
                latest,
                null,
                null,
                null
        );


        List<BookDTO> searchBooksWithImages = setImagePaths(books);
        model.addAttribute("searchBooksWithImages", searchBooksWithImages);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

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


    /**
     * 주어진 categories 리스트에서 categoryId에 해당하는 카테고리와 그 하위 서브 카테고리들의 categoryId를 추출하는 메서드
     *
     * @param categories 카테고리 리스트
     * @param categoryId 찾고자 하는 카테고리 ID
     * @return 해당 카테고리와 서브 카테고리들의 categoryId 리스트
     */
    public static List<Long> extractCategoryIds(List<CategoryDTO> categories, Long categoryId) {
        List<Long> categoryIds = new ArrayList<>();

        for (CategoryDTO category : categories) {
            // 카테고리가 부모 카테고리인 경우 (즉, categoryId가 상위 카테고리의 ID인 경우)
            if (category.getCategoryId().equals(categoryId)) {
                categoryIds.add(category.getCategoryId()); // 부모 카테고리 ID 추가

                // 서브 카테고리가 존재하면 그들의 ID를 추가
                if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                    for (CategoryDTO subCategory : category.getSubCategories()) {
                        categoryIds.add(subCategory.getCategoryId()); // 서브 카테고리 ID 추가
                    }
                }
                break; // 부모 카테고리 찾으면 더 이상 탐색하지 않음
            }

            // 카테고리가 자식 카테고리인 경우 (즉, categoryId가 서브 카테고리의 ID인 경우)
            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                for (CategoryDTO subCategory : category.getSubCategories()) {
                    // 자식 카테고리 ID가 일치하면 해당 자식 카테고리만 반환
                    if (subCategory.getCategoryId().equals(categoryId)) {
                        categoryIds.add(subCategory.getCategoryId()); // 자식 카테고리 ID만 추가
                        break; // 자식 카테고리 찾으면 더 이상 탐색하지 않음
                    }
                }
            }
        }

        return categoryIds;
    }
}
