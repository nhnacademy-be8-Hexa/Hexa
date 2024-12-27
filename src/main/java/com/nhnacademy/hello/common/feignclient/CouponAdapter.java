package com.nhnacademy.hello.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "hexa-gateway", contextId = "couponAdapter")
public interface CouponAdapter {


}
