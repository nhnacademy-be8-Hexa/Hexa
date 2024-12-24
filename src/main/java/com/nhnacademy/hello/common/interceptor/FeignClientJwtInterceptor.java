package com.nhnacademy.hello.common.interceptor;

import com.nhnacademy.hello.common.properties.JwtProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class FeignClientJwtInterceptor implements RequestInterceptor {

    private final HttpServletRequest request;
    private final JwtProperties jwtProperties;

    public FeignClientJwtInterceptor(HttpServletRequest request, JwtProperties jwtProperties) {
        this.request = request;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        // 쿠키에서 토큰 가져오기
        String jwtToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        // 헤더에 토큰 추가
        if(jwtToken != null) {
            requestTemplate.header(jwtProperties.getHeaderString(),
                    jwtProperties.getTokenPrefix() + " " + jwtToken);
        }

    }

}
