package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "hexa-gateway")
public interface HexaGateway {

    // auth api
    // login request (id, password) 를 보내고 토큰을 받음
    @PostMapping("/api/auth/login")
    public String login(@RequestBody LoginRequest request);


}
