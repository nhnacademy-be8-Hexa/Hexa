package com.nhnacademy.hello.controller.book;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookAdapter bookAdapter;
    private final ReviewAdapter reviewAdapter;

    @GetMapping("/book/{bookId}")
    public String bookDetail(
            @PathVariable("bookId") Long bookId,
            Model model
    ){
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

        // 리뷰의 평점 평균 (별 0.5 ~ 5.0)
        // todo

        // 리뷰 갯수
        // todo
        int reviewCount = 0;
        model.addAttribute("reviewCount", reviewCount);

        return "book/bookDetail";
    }

}
