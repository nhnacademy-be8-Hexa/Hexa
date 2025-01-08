package com.nhnacademy.hello.controller.index;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.dto.book.BookDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final BookAdapter bookAdapter;
    private final CategoryAdapter categoryAdapter;
    private final SetImagePathsUtils setImagePathsUtils;

    @GetMapping(value = {"/index.html", "/"})
    public String index(Model model) {

        // 베스트셀러
        List<BookDTO> best_seller_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, null, true, null, null, null, null, null
        );
        List<BookDTO> bestSellerWithImages = setImagePathsUtils.setImagePaths(best_seller_books);
        model.addAttribute("best_seller_books", bestSellerWithImages);

        // 가장 많이 클릭된 책 (조회수)
        List<BookDTO> most_viewed_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, true, null, null, null, null, null, null
        );
        List<BookDTO> mostViewedWithImages = setImagePathsUtils.setImagePaths(most_viewed_books);
        model.addAttribute("most_viewed_books", mostViewedWithImages);

        // 좋아요가 가장 많은 책
        List<BookDTO> most_liked_books = bookAdapter.getBooks(
                0, 6, "", null, null, null, null, null, null, true, null, null, null, null
        );
        List<BookDTO> mostLikedWithImages = setImagePathsUtils.setImagePaths(most_liked_books);
        model.addAttribute("most_liked_books", mostLikedWithImages);
        

        return "index/index";
    }


}
