package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.AladinBookDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hexa-gateway", contextId = "aladinApiAdapter")
public interface AladinApiAdapter {

    @GetMapping("/api/aladinApi")
    List<AladinBookDTO> searchBooks(@RequestParam(required = false) String query);
    
}
