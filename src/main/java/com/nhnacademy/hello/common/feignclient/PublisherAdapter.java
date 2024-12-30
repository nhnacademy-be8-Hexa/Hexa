package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.PublisherRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "publisherAdapter")
public interface PublisherAdapter {
    @GetMapping("/api/publishers")
    List<PublisherRequestDTO> getPublishers();
}
