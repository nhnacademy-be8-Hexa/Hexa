package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.BookDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hexa-gateway", contextId = "ElasticSearchAdapter")
public interface ElasticSearchAdapter {
    @GetMapping("/api/search")
    List<BookDTO> searchBooks(@RequestParam("page") int page, @RequestParam("size") int size,
                              @RequestParam("search") String search);

    @GetMapping("/api/search/total")
    ResponseEntity<Long> getTotalSearchBooks(@RequestParam("search") String search);
}
