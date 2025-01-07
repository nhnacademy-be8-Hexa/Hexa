package com.nhnacademy.hello.common.config;

import com.nhnacademy.hello.common.interceptor.FeignClientJwtInterceptor;
import com.nhnacademy.hello.common.properties.JwtProperties;
import feign.codec.Decoder;
import feign.codec.Encoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
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

    // 직렬화 문제로 추가
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringDecoder(messageConverters);
    }

    @Bean
    public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringEncoder(messageConverters);
    }
}
