package com.nhnacademy.hello.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hexa-gateway", contextId = "CartAdapter")
public interface CartAdapter {
    @GetMapping("/api/members/{memberId}/carts")
    ResponseEntity<String> getCart(
            @PathVariable String memberId);

    @PutMapping("/api/members/{memberId}/carts")
    ResponseEntity<String> setCart(
            @PathVariable String memberId,
            @RequestBody String cart);
}
