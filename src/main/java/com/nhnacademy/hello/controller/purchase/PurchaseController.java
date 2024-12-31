package com.nhnacademy.hello.controller.purchase;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final BookAdapter bookAdapter;

    @GetMapping("/purchase")
    public String purchaseCartItem(
            @RequestParam List<Long> bookIds,
            Model model
    ) {

        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("bookIds 가 전달되지 않았습니다.");
        }

        // 전달받은 북 ids 로 책 리스트 받아와서 model 로 전달
        List<BookDTO> bookList = bookAdapter.getBooksByIds(bookIds);
        model.addAttribute("bookList", bookList);



        return "purchase/purchase";
    }

}
