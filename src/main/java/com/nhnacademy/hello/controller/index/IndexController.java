package com.nhnacademy.hello.controller.index;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final BookAdapter bookAdapter;

    @GetMapping(value = {"/index.html","/"})
    public String index(
            Model model
    ){

        // 베스트셀러
        List<BookDTO> best_seller_books = bookAdapter.getBooks(
                0,
                6,
                "",
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null
        );

        model.addAttribute("best_seller_books", best_seller_books);

        // 가장 많이 클릭된 책 (조회수)
        List<BookDTO> most_viewed_books = bookAdapter.getBooks(
                0,
                6,
                "",
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null
        );

        model.addAttribute("most_viewed_books", most_viewed_books);

        // 좋아요가 가장 많은 책
        List<BookDTO> most_liked_books = bookAdapter.getBooks(
                0,
                6,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null
        );

        model.addAttribute("most_liked_books", most_liked_books);


        return "index/index";
    }
}
