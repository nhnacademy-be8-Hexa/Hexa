package com.nhnacademy.hello.common.api;

import com.nhnacademy.hello.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AuthApi", url = "http://localhost:8100/api/auth")
public interface AuthApi {

    // login request (id, password) 를 보내고 토큰을 받음
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request);

}
