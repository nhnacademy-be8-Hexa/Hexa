package com.nhnacademy.hello.controller.login;

import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.dto.member.LoginRequest;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    private final JwtProperties jwtProperties;
    private final MemberAdapter memberAdapter;

    @Value("${jwt_token_cookie_secure}")
    private String secure;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login/process")
    public String process(
            @ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response,
            Model model
            ) throws IOException {
        // 인증 서버에 로그인 요청을 하고 토큰을 받는다.
        String token = memberAdapter.login(loginRequest);

        // 토큰 예외처리 (로그인 실패)
        if(token == null) {
            log.error("Authorization failure id: {}", loginRequest.id());
            model.addAttribute("error", "로그인 실패!");
            return "login";
        }

        // 토큰을 쿠키에 저장한다
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtProperties.getExpirationTime());
        cookie.setSecure(Boolean.parseBoolean(secure));
        response.addCookie(cookie);

        // 로그인 시간 업데이트 요청을 보낸다
        memberAdapter.loginMember(loginRequest.id());


        // 로그인 후 홈페이지로 이동
        return "redirect:/?clearLocalCart=true";
    }


}
