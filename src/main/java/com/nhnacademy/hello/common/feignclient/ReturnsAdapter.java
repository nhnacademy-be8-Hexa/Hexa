package com.nhnacademy.hello.common.feignclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "returnsAdapter")
public interface ReturnsAdapter {
    @PostMapping("/api/returns")
    public ResponseEntity<ReturnsDTO> createReturns(@Valid @RequestBody ReturnsRequestDTO returnsRequestDTO);
    @GetMapping("/api/returns")
    public List<ReturnsDTO> getAllReturns(Pageable pageable);

    @GetMapping("/api/returns/returnReason/{ReturnReasonId}")
    public ReturnsDTO getReturnsByReturnsReasonId(@PathVariable Long ReturnReasonId);

    @GetMapping("/api/returns/order/{orderId}")
    public ReturnsDTO getReturnsByOrderId(@PathVariable Long orderId);
    @GetMapping("/api/returns/member/{memberId}")
    public ReturnsDTO getReturnsByMemberId(@PathVariable String memberId);

    @PutMapping("/api/returns/order/{orderId}")
    public ResponseEntity<ReturnsDTO> updateReturns(@PathVariable Long orderId, @Valid @RequestBody ReturnsRequestDTO returnsRequestDTO);


    @DeleteMapping("/api/returns/order/{orderId}")
    public void deleteReturns(@PathVariable Long orderId);

}
