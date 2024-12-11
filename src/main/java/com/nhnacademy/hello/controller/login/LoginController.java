package com.nhnacademy.hello.controller.login;

import com.nhnacademy.hello.common.api.AuthApi;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.JwtUtils;
import com.nhnacademy.hello.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final AuthApi authApi;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login/process")
    public String process(
            @ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response) throws IOException {
        // 인증 서버에 로그인 요청을 하고 토큰을 받는다.
        String token = authApi.login(loginRequest);

        // 여기로 넘어 왔다면 로그인 성공했고 토큰을 받은것

        // 토큰을 쿠키에 저장한다
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtProperties.getExpirationTime());
        response.addCookie(cookie);

        // 토큰을 파싱 해서 security context에 인증 정보를 저장한다
        // (customUserDetails 를 이용해서 security 인증 절차를 생략하고 직접 구현하는 것)
        String username = jwtUtils.getUsernameFromToken(token);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(jwtUtils.getRoleFromToken(token)));

        // 인증 객체 생성 (customUserDetails 에서 늘 생성하던거)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        // 컨텍스트에 인증 설정 (security 인증)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/";
    }

}
