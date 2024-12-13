package com.nhnacademy.hello.controller.login;

import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.dto.LoginRequest;
import com.nhnacademy.hello.common.feignclient.HexaGateway;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final JwtProperties jwtProperties;
    private final HexaGateway hexaGateway;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login/process")
    public String process(
            @ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response) throws IOException {
        // 인증 서버에 로그인 요청을 하고 토큰을 받는다.
        String token = hexaGateway.login(loginRequest);

        // todo 토큰 예외처리

        // 토큰을 쿠키에 저장한다
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtProperties.getExpirationTime());
        response.addCookie(cookie);

        // 로그인 후 홈페이지로 이동
        return "redirect:/";
    }


}
