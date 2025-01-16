package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.AladinBookDTO;
import com.nhnacademy.hello.dto.book.AladinBookRequestDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hexa-gateway", contextId = "aladinApiAdapter")
public interface AladinApiAdapter {

    @GetMapping("/api/aladinApi")
    List<AladinBookDTO> searchBooks(@RequestParam(required = false) String query);

    @PostMapping("/api/aladinApi")
    ResponseEntity<BookDTO> createAladinBook(@RequestBody AladinBookRequestDTO aladinBookRequestDTO);
    
}
