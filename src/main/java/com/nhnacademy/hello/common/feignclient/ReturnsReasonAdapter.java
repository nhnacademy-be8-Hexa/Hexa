package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "returnsReasonAdapter")
public interface ReturnsReasonAdapter {

    @PostMapping("/api/returnsReason")
    public ResponseEntity<ReturnsReasonDTO> createReturnsReason(
            @Valid @RequestBody ReturnsReasonRequestDTO returnsReasonRequestDTO);


    @GetMapping("/api/returnsReason")
    public List<ReturnsReasonDTO> getAllReturnsReasons();

    @GetMapping("/api/returnsReason/{returnsReasonId}")
    public ReturnsReasonDTO getReturnsReasonById(
            @PathVariable Long returnsReasonId);

    @PutMapping("/api/returnsReason/{returnsReasonId}")
    public ResponseEntity<ReturnsReasonDTO> updateReturnsReason(
            @PathVariable Long returnsReasonId,
            @Valid @RequestBody ReturnsReasonRequestDTO returnsReasonRequestDTO);

    @DeleteMapping("/api/returnsReason/{returnsReasonId}")
    public void deleteReturnsReason(
            @PathVariable Long returnsReasonId);

}
