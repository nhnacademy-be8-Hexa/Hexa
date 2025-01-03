package com.nhnacademy.hello.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

public class TossPaymentInterceptor implements RequestInterceptor {

    @Value("${toss.secret.key}")
    private String secretKey;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Basic " + secretKey);
        requestTemplate.header("Content-Type", "application/json");
    }

}
