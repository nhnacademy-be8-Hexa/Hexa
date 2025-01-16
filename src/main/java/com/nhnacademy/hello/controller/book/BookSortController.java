package com.nhnacademy.hello.controller.book;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/bestsellers")
    public String bestsellers(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> bestSellerBooks = bookAdapter.getBooks(
                adjustedPage, 18, "", null, null, null, null, null, true, null, null, null, null, null
        );

        Long totalBooks = bookAdapter.getTotalBooks(
                null,
                null,
                null,
                null
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / 18);

        bestSellerBooks = setImagePathsUtils.setImagePaths(bestSellerBooks);

        model.addAttribute("searchBooksWithImages", bestSellerBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", 18);
        model.addAttribute("path", "/bestsellers");

        return "book/bookSort";
    }

    @GetMapping("/newarrivals")
    public String newarrivals(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> newArrivalsBooks = bookAdapter.getBooks(
                adjustedPage, 18, "", null, null, null, null, null, null, null, true, null, null, null
        );

        Long totalBooks = bookAdapter.getTotalBooks(
                null,
                null,
                null,
                null
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / 18);

        newArrivalsBooks = setImagePathsUtils.setImagePaths(newArrivalsBooks);

        model.addAttribute("searchBooksWithImages", newArrivalsBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", 18);
        model.addAttribute("path", "/newarrivals");

        return "book/bookSort";
    }

    @GetMapping("/manyreview")
    public String manyreview(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;
        List<BookDTO> manyReivewsBooks = bookAdapter.getBooks(
                adjustedPage, 18, "", null, null, null, null, null, null, null, null, null, null, true
        );

        Long totalBooks = bookAdapter.getTotalBooks(
                null,
                null,
                null,
                null
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / 18);

        manyReivewsBooks = setImagePathsUtils.setImagePaths(manyReivewsBooks);

        model.addAttribute("searchBooksWithImages", manyReivewsBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", 18);
        model.addAttribute("path", "/manyreview");

        return "book/bookSort";
    }

    @GetMapping("/name")
    public String name(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, @RequestParam String sort, Model model) {
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;
        List<BookDTO> sortByTitleBooks = null;
        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("desc")) {
                sortByTitleBooks = bookAdapter.getBooks(
                        adjustedPage, 18, "", null, null, null, null, null, null, null, null, true, null, null
                );
            } else {
                sortByTitleBooks = bookAdapter.getBooks(
                        adjustedPage, 18, "", null, null, null, null, null, null, null, null, null, true, null
                );
            }
        }
        sortByTitleBooks = setImagePathsUtils.setImagePaths(sortByTitleBooks);

        Long totalBooks = bookAdapter.getTotalBooks(
                null,
                null,
                null,
                null
        ).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / 18);

        sortByTitleBooks = setImagePathsUtils.setImagePaths(sortByTitleBooks);

        model.addAttribute("searchBooksWithImages", sortByTitleBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", 18);
        model.addAttribute("sort", sort);
        model.addAttribute("path", "/name");

        return "book/bookSort";
    }

    @GetMapping("/tag/{tagId}")
    public String tag(
            @PathVariable("tagId") Long tagId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model
    ){
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        List<BookDTO> tagBooks = booKTagAdapter.getBooksByTag(tagId, adjustedPage, 18, "").getBody();

        int totalBooks = booKTagAdapter.getBookCountByTag(tagId).getBody();

        int totalPages = (int) Math.ceil((double) totalBooks / 18);

        tagBooks = setImagePathsUtils.setImagePaths(tagBooks);

        TagDTO tag = tagAdapter.getTagById(tagId).getBody();

        model.addAttribute("tagName", tag.getTagName());

        model.addAttribute("searchBooksWithImages", tagBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", 18);

        return "book/bookSort";
    }

}
