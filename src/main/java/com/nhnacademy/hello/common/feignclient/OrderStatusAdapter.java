package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import com.nhnacademy.hello.dto.order.OrderStatusRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "orderStatusAdapter")
public interface OrderStatusAdapter {

    @GetMapping("/api/orderStatus")
    public List<OrderStatusDTO> getAllOrderStatus();

    @PostMapping("/api/orderStatus")
    public ResponseEntity<OrderStatusDTO> createOrderStatus(
            @Valid @RequestBody OrderStatusRequestDTO orderStatusRequestDTO);

    @GetMapping("/api/orderStatus/{orderStatusId}")
    public OrderStatusDTO getOrderStatus(
            @PathVariable Long orderStatusId);

    @PutMapping("/api/orderStatus/{orderStatusId}")
    public ResponseEntity<OrderStatusDTO> updateOrderStatus(
            @PathVariable Long orderStatusId,
            @RequestBody OrderStatusRequestDTO orderStatusRequestDTO);

    @DeleteMapping("/api/orderStatus/{orderStatusId}")
    public ResponseEntity<OrderStatusDTO> deleteOrderStatus(
            @PathVariable Long orderStatusId);

}
