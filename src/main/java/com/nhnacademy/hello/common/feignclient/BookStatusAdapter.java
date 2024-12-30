package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.BookStatusRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "bookStatusAdapter")
public interface BookStatusAdapter {
    @GetMapping("/api/bookStatuses")
    List<BookStatusRequestDTO> getBookStatus(@RequestParam("bookId") String bookId);
}
