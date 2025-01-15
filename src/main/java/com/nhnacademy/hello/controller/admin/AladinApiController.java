package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.AladinApiAdapter;
import com.nhnacademy.hello.dto.book.AladinBookDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/aladinApi")
@RequiredArgsConstructor
public class AladinApiController {

    private final AladinApiAdapter aladinApiAdapter;

    @GetMapping
    public String aladinApi(@RequestParam(required = false) String query, Model model) {
        List<AladinBookDTO> books = aladinApiAdapter.searchBooks(query);
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        return "/admin/aladinBookSearch";
    }
}
