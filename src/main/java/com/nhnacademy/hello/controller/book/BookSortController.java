package com.nhnacademy.hello.controller.book;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BookSortController {
    private final BookAdapter bookAdapter;
    private final SetImagePathsUtils setImagePathsUtils;

    private final TagAdapter tagAdapter;
    private final BooKTagAdapter booKTagAdapter;

    private final int SIZE = 18;

    @GetMapping("/bestsellers")
    public String bestsellers(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> bestSellerBooks = bookAdapter.getBooks(
                adjustedPage, SIZE, List.of("bookSellCount,desc", "bookId,asc"),
                null, null, null,
                null, null, null
        );

        ResponseEntity<Long> response = bookAdapter.getTotalBooks(null, null, null, null);
        Long totalBooks = (response != null && response.getBody() != null) ? response.getBody() : 0L;

        int totalPages = (int) Math.ceil((double) totalBooks / SIZE);

        bestSellerBooks = setImagePathsUtils.setImagePaths(bestSellerBooks);

        model.addAttribute("searchBooksWithImages", bestSellerBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", SIZE);

        return "book/bookSort";
    }

    @GetMapping("/newarrivals")
    public String newarrivals(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> newArrivalsBooks = bookAdapter.getBooks(
                adjustedPage, SIZE, List.of("bookPubDate,desc", "bookId,desc"),
                null, null, null,
                null, null, null
        );

        ResponseEntity<Long> response = bookAdapter.getTotalBooks(null, null, null, null);
        Long totalBooks = (response != null && response.getBody() != null) ? response.getBody() : 0L;

        int totalPages = (int) Math.ceil((double) totalBooks / SIZE);

        newArrivalsBooks = setImagePathsUtils.setImagePaths(newArrivalsBooks);

        model.addAttribute("searchBooksWithImages", newArrivalsBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", SIZE);

        return "book/bookSort";
    }

    @GetMapping("/manyreview")
    public String manyreview(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> manyReivewsBooks = bookAdapter.getBooks(
                adjustedPage, SIZE, List.of("bookSellCount,desc","bookId,asc"),
                null, null, null,
                null, null, true
        );

        ResponseEntity<Long> response = bookAdapter.getTotalBooks(null, null, null, null);
        Long totalBooks = (response != null && response.getBody() != null) ? response.getBody() : 0L;

        int totalPages = (int) Math.ceil((double) totalBooks / SIZE);

        manyReivewsBooks = setImagePathsUtils.setImagePaths(manyReivewsBooks);

        model.addAttribute("searchBooksWithImages", manyReivewsBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", SIZE);

        return "book/bookSort";
    }

    @GetMapping("/name")
    public String name(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam String sort,
            Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> sortByTitleBooks = bookAdapter.getBooks(
                adjustedPage, SIZE, List.of(sort),
                null, null, null,
                null, null, null
        );

        sortByTitleBooks = setImagePathsUtils.setImagePaths(sortByTitleBooks);

        ResponseEntity<Long> response = bookAdapter.getTotalBooks(null, null, null, null);
        Long totalBooks = (response != null && response.getBody() != null) ? response.getBody() : 0L;

        int totalPages = (int) Math.ceil((double) totalBooks / SIZE);

        sortByTitleBooks = setImagePathsUtils.setImagePaths(sortByTitleBooks);

        model.addAttribute("searchBooksWithImages", sortByTitleBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", SIZE);
        model.addAttribute("sort", sort);

        return "book/bookSort";
    }

    @GetMapping("/tag/{tagId}")
    public String tag(
            @PathVariable("tagId") Long tagId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model
    ){
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> tagBooks = booKTagAdapter.getBooksByTag(tagId, adjustedPage, SIZE, "bookTagId,desc").getBody();

        int totalBooks = booKTagAdapter.getBookCountByTag(tagId).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / SIZE);

        tagBooks = setImagePathsUtils.setImagePaths(tagBooks);

        TagDTO tag = tagAdapter.getTagById(tagId).getBody();

        model.addAttribute("tagName", tag.getTagName());

        model.addAttribute("searchBooksWithImages", tagBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", SIZE);

        return "book/bookSort";
    }

}
