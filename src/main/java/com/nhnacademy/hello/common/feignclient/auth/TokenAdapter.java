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

    // 리프레시 토큰 생성
    @PostMapping("/api/auth/refreshTokenRedis")
    ResponseEntity<Void> saveToken(@RequestHeader("Authorization") String refreshToken);

    // 리프레시 토큰 가져오기
    @GetMapping("/api/auth/refreshTokenRedis")
    String getServerToken(@RequestHeader("Authorization") String refreshToken);

    // 리프레시 토큰 삭제
    @DeleteMapping("/api/auth/refreshTokenRedis")
    ResponseEntity<Void> deleteToken(@RequestHeader("Authorization") String refreshToken);

    // 토큰 재발행(접근, 리프레시)
    @PostMapping("/api/auth/reissue")
    AccessRefreshTokenDTO reissueAccessRefreshToken(@RequestHeader("Authorization") String refreshToken);

}
