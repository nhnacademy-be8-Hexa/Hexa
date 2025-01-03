package com.nhnacademy.hello.common.config;

import com.nhnacademy.hello.common.interceptor.FeignClientJwtInterceptor;
import com.nhnacademy.hello.common.interceptor.TossPaymentInterceptor;
import com.nhnacademy.hello.common.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class FeignConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public FeignClientJwtInterceptor feignClientJwtInterceptor(HttpServletRequest request) {
        return new FeignClientJwtInterceptor(request, jwtProperties);
    }

}
