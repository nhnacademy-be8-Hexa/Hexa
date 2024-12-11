package com.nhnacademy.hello.feignclient;

import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "hexa-gateway")
public interface HexaGateway {

}
