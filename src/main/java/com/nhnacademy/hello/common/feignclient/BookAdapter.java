package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "hexa-book")
public interface BookAdapter {

    //테스트 용 - 전체 도서 페이징

    @GetMapping("/api/books")
    List<BookDTO> getBooks(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "search", required = false) String search
    );}
