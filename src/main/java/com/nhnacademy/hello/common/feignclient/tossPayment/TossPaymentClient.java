package com.nhnacademy.hello.common.feignclient.tossPayment;

import com.nhnacademy.hello.common.config.TossPaymentConfig;
import com.nhnacademy.hello.common.interceptor.TossPaymentInterceptor;
import com.nhnacademy.hello.dto.purchase.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tossPaymentClient",
        url = "https://api.tosspayments.com",
        configuration = TossPaymentConfig.class
)
public interface TossPaymentClient {

    @PostMapping("/v1/payments/confirm")
    ResponseEntity<?> confirm(
            @RequestBody PaymentRequest paymentRequest);

}
