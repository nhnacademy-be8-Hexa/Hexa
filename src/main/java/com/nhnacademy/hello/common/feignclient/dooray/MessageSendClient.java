package com.nhnacademy.hello.common.feignclient.dooray;

import com.nhnacademy.hello.dto.dooray.MessagePayloadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "messageSendClient", url = "https://hook.dooray.com/services")
public interface MessageSendClient {
    @PostMapping("/{serviceId}/{botId}/{botToken}")
    String sendMessage(@RequestBody MessagePayloadDTO messagePayload,
                       @PathVariable Long serviceId, @PathVariable Long botId, @PathVariable String botToken);
}
//https://nhnacademy.dooray.com/services/3204376758577275363/3972679502704436132/xxUSYaNnQHCzvfUNfWB8jA
