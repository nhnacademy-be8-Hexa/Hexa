package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.delivery.DeliveryRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "deliveryAdapter")
public interface DeliveryAdapter {
    @PostMapping("/api/deliveries")
    public ResponseEntity<Void> createDelivery(@RequestBody @Valid DeliveryRequestDTO deliveryRequestDTO);

    @GetMapping("/api/deliveries")
    public List<DeliveryDTO> getAllDelivery(Pageable pageable, HttpServletRequest request);

    @GetMapping("/api/orders/{orderId}/deliveries")
    public DeliveryDTO getDelivery(@PathVariable Long orderId);

    @GetMapping("/api/members/{memberId}/deliveries")
    public List<DeliveryDTO> getDeliveryByMemberId(@PathVariable String memberId, Pageable pageable, HttpServletRequest request);

    @PutMapping("/api/orders/{orderId}/deliveries")
    public ResponseEntity<Void> updateDelivery(@PathVariable Long orderId, @RequestBody @Valid DeliveryRequestDTO deliveryRequestDTO);


}
