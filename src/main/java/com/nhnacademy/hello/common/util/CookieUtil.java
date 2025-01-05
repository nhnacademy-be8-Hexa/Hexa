package com.nhnacademy.hello.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {


    // 쿠키 생성 및 설정
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 기본적으로 '/'로 설정 (어플리케이션의 루트 경로)
        cookie.setHttpOnly(true); // Java Script 에서 쿠키에 접근하지 못하게 설정하여 XSS 공격을 방지
        cookie.setMaxAge(maxAge); // 만료 시간 설정 (초 단위)
        cookie.setSecure(true); // HTTPS 연결에서만 전송할지 설정
        cookie.setAttribute("SameSite", "Strict"); // 쿠키가 이 사이트 이외에서는 요청에 포함되지 않게 설정 CSRF 공격 방지
        response.addCookie(cookie);
    }

    // AccessToken 쿠키 생성
    public static void addResponseAccessTokenCookie(HttpServletResponse response, String accessToken, int maxAge) {
        addCookie(response, "accessToken", accessToken, maxAge);
    }

    // RefreshToken 쿠키 생성
    public static void addResponseRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        addCookie(response, "refreshToken", refreshToken, maxAge);
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
