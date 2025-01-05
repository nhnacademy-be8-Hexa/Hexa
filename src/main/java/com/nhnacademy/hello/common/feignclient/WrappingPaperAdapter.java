package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.order.WrappingPaperDTO;
import com.nhnacademy.hello.dto.order.WrappingPaperRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "wrappingPaperAdapter", path = "/api/wrappingPaper")
public interface WrappingPaperAdapter {

    @GetMapping
    public List<WrappingPaperDTO> getAllWrappingPapers();

    @PostMapping
    public ResponseEntity<WrappingPaperDTO> createWrappingPaper(
            @Valid @RequestBody WrappingPaperRequestDTO wrappingPaperRequestDTO);


    @GetMapping("/{wrappingPaperId}")
    public WrappingPaperDTO getWrappingPaper(
            @PathVariable Long wrappingPaperId);

    @PutMapping("/{wrappingPaperId}")
    public ResponseEntity<WrappingPaperDTO> updateWrappingPaper(
            @PathVariable Long wrappingPaperId,
            @Valid @RequestBody
            WrappingPaperRequestDTO wrappingPaperRequestDTO);

    @DeleteMapping("/{wrappingPaperId}")
    public ResponseEntity<WrappingPaperDTO> deleteWrappingPaper(
            @PathVariable Long wrappingPaperId);

}
