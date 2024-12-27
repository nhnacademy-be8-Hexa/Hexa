package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.cart.CartDTO;
import com.nhnacademy.hello.dto.cart.CartRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "cartAdapter")
public interface CartAdapter {

    @GetMapping("/api/members/{memberId}/carts/{cartId}")
    public CartDTO getCart(
            @PathVariable String memberId,
            @PathVariable Long cartId
    );

    @GetMapping("/api/members/{memberId}/carts")
    public List<CartDTO> getCartByMemberId(
            @PathVariable String memberId
    );

    @PostMapping("/api/carts")
    public ResponseEntity<Void> createCart(
            @RequestBody @Valid CartRequestDTO cartRequestDTO);

    @DeleteMapping("/api/members/{memberId}/carts/{cartId}")
    public void deleteCart(
            @PathVariable String memberId,
            @PathVariable Long cartId
    );

    @DeleteMapping("/api/members/{memberId}/carts")
    public ResponseEntity<Void> clearCartByMember(
            @PathVariable String memberId
    );

    @PutMapping("/api/carts/{cartId}/quantity")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @PathVariable Long cartId,
            @RequestBody CartRequestDTO cartRequestDto);


}
