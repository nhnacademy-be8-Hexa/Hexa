package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.order.OrderStatusDTO;
import com.nhnacademy.hello.dto.order.OrderStatusRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="hexa-gateway", contextId = "orderStatusAdapter")
public interface OrderStatusAdapter {

    @GetMapping
    public List<OrderStatusDTO> getAllOrderStatus();

    @PostMapping
    public ResponseEntity<OrderStatusDTO> createOrderStatus(
            @Valid @RequestBody OrderStatusRequestDTO orderStatusRequestDTO);

    @GetMapping("/{orderStatusId}")
    public OrderStatusDTO getOrderStatus(
            @PathVariable Long orderStatusId);

    @PutMapping("/{orderStatusId}")
    public ResponseEntity<OrderStatusDTO> updateOrderStatus(
            @PathVariable Long orderStatusId,
            @RequestBody OrderStatusRequestDTO orderStatusRequestDTO);

    @DeleteMapping("/{orderStatusId}")
    public ResponseEntity<OrderStatusDTO> deleteOrderStatus(
            @PathVariable Long orderStatusId);

}
