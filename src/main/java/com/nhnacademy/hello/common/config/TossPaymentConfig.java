package com.nhnacademy.hello.common.config;


import com.nhnacademy.hello.common.interceptor.TossPaymentInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TossPaymentConfig {

    @Bean
    public TossPaymentInterceptor tossPaymentInterceptor() {
        return new TossPaymentInterceptor();
    }

}
