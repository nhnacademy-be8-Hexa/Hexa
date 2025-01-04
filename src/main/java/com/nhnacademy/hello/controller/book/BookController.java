package com.nhnacademy.hello.controller.book;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import java.util.List;

import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.image.ImageStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookAdapter bookAdapter;
    private final ReviewAdapter reviewAdapter;
    private final ImageStore imageStore;
    private final TagAdapter tagAdapter;

    @GetMapping("/book/{bookId}")
    public String bookDetail(
            @PathVariable("bookId") Long bookId,
            Model model
    ) {
        // 책 상세정보 페이지
        // 책 정보 전달
        BookDTO book = bookAdapter.getBook(bookId);
        model.addAttribute("book", book);

        // 책의 저자 리스트 전달
        List<String> authors = bookAdapter.getAuthors(bookId)
                .stream().map(AuthorDTO::authorName).toList();
        model.addAttribute("authors", authors);

        // 책의 좋아요 수
        Long likeCount = bookAdapter.getLikeCount(bookId).getBody();
        model.addAttribute("likeCount", likeCount);

        // todo
        bookAdapter.incrementBookView(bookId);

        // 리뷰의 평점 평균 (별 0.5 ~ 5.0)
        // todo

        // 리뷰 갯수
        // todo
        int reviewCount = 0;
        model.addAttribute("reviewCount", reviewCount);


        // 책 섬네일

        String imageName = "bookThumbnail_" + book.bookId();
        List<String> imagePaths = imageStore.getImage(imageName);
        String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                imagePaths.get(0) : "/images/default-book.jpg"; // 기본 이미지 경로로 수정

        model.addAttribute("thumbnailImage", imagePath);

        //책의 태그
        List<TagDTO> tags = tagAdapter.getAllTags().getBody();
        model.addAttribute("tags", tags);

        return "book/bookDetail";
    }

//    @GetMapping("/book")
//    public String bookList(
//            // 검색 파라미터
//            @RequestParam(value = "search", required = false) String search,
//
//            // 페이징 파라미터
//            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
//            @RequestParam(value = "size", required = false, defaultValue = "9") Integer size,
//
//            // 정렬 파라미터
//            @RequestParam(value = "sort", required = false, defaultValue = "title") String sort,
//
//            // 추가적인 검색 및 정렬 파라미터
//            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
//            @RequestParam(value = "publisherName", required = false) String publisherName,
//            @RequestParam(value = "authorName", required = false) String authorName,
//            @RequestParam(value = "sortByView", required = false, defaultValue = "false") Boolean sortByView,
//            @RequestParam(value = "sortBySellCount", required = false, defaultValue = "false") Boolean sortBySellCount,
//            @RequestParam(value = "sortByLikeCount", required = false, defaultValue = "false") Boolean sortByLikeCount,
//            @RequestParam(value = "latest", required = false, defaultValue = "false") Boolean latest,
//
//            Model model
//    ) {
//        // 페이지 번호 조정 (0 기반 인덱스 사용 시)
//        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;
//
//        // bookAdapter.getBooks 메서드 호출 시 모든 파라미터 전달
//        List<BookDTO> books = bookAdapter.getBooks(
//                adjustedPage,
//                size,
//                sort,
//                search,
//                categoryIds,
//                publisherName,
//                authorName,
//                sortByView,
//                sortBySellCount,
//                sortByLikeCount,
//                latest
//        );
//
//        // 모델에 데이터 추가
//        model.addAttribute("books", books);
//
//        // 뷰 이름 반환 (예: "bookList")
//        return "book/books";
//    }
}
