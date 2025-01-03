package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.AuthorRequestDTO;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "hexa-gateway", contextId = "authorAdapter")
public interface AuthorAdapter {

    // 작가 생성
    @PostMapping("/api/authors")
    AuthorDTO createAuthor(@RequestBody AuthorRequestDTO authorRequestDTO);

    // 특정 도서의 작가 목록 조회
    @GetMapping("/api/books/{bookId}/authors")
    AuthorDTO[] getAuthorsByBookId(@PathVariable Long bookId);
}
