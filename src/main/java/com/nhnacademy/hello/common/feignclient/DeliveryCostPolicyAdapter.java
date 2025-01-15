package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyDTO;
import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="hexa-gateway", contextId = "deliveryCostPolicyAdapter")
public interface DeliveryCostPolicyAdapter {
    @GetMapping("/api/delivery-cost-policy/recent")
    ResponseEntity<DeliveryCostPolicyDTO> getRecent();

    @GetMapping("/api/delivery-cost-policy/all")
    public ResponseEntity<List<DeliveryCostPolicyDTO>> getAll(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );


    @PostMapping("/api/delivery-cost-policy")
    public ResponseEntity<?> create(@RequestBody DeliveryCostPolicyRequest deliveryCostPolicyRequest);
}
