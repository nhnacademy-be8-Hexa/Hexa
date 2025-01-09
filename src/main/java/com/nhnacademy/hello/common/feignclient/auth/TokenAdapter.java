package com.nhnacademy.hello.common.feignclient.auth;

import com.nhnacademy.hello.dto.jwt.AccessRefreshTokenDTO;
import com.nhnacademy.hello.dto.member.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "hexa-gateway", contextId = "tokenAdapter")
public interface TokenAdapter {


    // auth api
    // login request (id, password) 를 보내고 토큰을 받음
    @PostMapping("/api/auth/login")
    public AccessRefreshTokenDTO login(@RequestBody LoginRequest request);


    // 보낼떄 Bearer <토큰> 형식으로 보내야함

    // 리프레시 토큰 블랙리스트 등록
    @PostMapping("/api/auth/refreshTokenBlacklists")
    ResponseEntity<Void> addToBlackListToken(@RequestHeader("Authorization") String refreshToken);

    // 리프레시 토큰 블랙리스트에 있는지 여부
    @GetMapping("/api/auth/refreshTokenBlacklists")
    Boolean isTokenBlackListed(@RequestHeader("Authorization") String refreshToken);

    // 리프레시 토큰 블랙리스트에서 삭제
    @DeleteMapping("/api/auth/refreshTokenBlacklists")
    ResponseEntity<Void> deleteToBlackListToken(@RequestHeader("Authorization") String refreshToken);

    // 토큰 재발행(접근, 리프레시)
    @PostMapping("/api/auth/reissue")
    AccessRefreshTokenDTO reissueAccessRefreshToken(@RequestHeader("Authorization") String refreshToken);

}
