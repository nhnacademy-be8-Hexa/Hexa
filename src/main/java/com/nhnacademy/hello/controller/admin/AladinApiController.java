package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.AladinApiAdapter;
import com.nhnacademy.hello.common.feignclient.AuthorAdapter;
import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.PublisherAdapter;
import com.nhnacademy.hello.dto.book.AladinBookDTO;
import com.nhnacademy.hello.dto.book.AladinBookRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/aladinApi")
@RequiredArgsConstructor
public class AladinApiController {

    private final AladinApiAdapter aladinApiAdapter;
    private final BookAdapter bookAdapter;
    private final AuthorAdapter authorAdapter;
    private final PublisherAdapter publisherAdapter;

    @GetMapping
    public String aladinApi(@RequestParam(required = false) String query, Model model) {
        List<AladinBookDTO> books = aladinApiAdapter.searchBooks(query);
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        return "admin/aladinBookSearch";
    }

    @PostMapping
    public ResponseEntity<?> createAladinBook(@Valid @RequestBody AladinBookRequestDTO aladinBookRequestDTO) {
        aladinApiAdapter.createAladinBook(aladinBookRequestDTO);
        return ResponseEntity.ok().build();
    }
}
