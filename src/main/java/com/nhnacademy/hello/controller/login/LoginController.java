package com.nhnacademy.hello.controller.login;

import com.nhnacademy.hello.common.api.AuthApi;
import com.nhnacademy.hello.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final AuthApi authApi;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login/process")
    public String process(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        // 로그인 요청을 하고 토큰을 받는다.
        //todo 페인클라를 쏴서 인증 서버와 통신해야 한다
        String token = authApi.login(loginRequest);

        // 토큰을 쿠키에 저장한다
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return "redirect:/";
    }

}
