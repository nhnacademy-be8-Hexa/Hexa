package com.nhnacademy.hello.service;

import com.nhnacademy.hello.common.feignclient.dooray.MessageSendClient;
import com.nhnacademy.hello.dto.dooray.MessagePayloadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSendService {
    @Autowired
    private MessageSendClient messageSendClient;

    public String sendMessage(String content) {
        MessagePayloadDTO messageRequest = new MessagePayloadDTO();
        messageRequest.setBotName("Notification");
        messageRequest.setText(content);

        return messageSendClient.sendMessage(messageRequest, 3204376758577275363L, 3972679502704436132L, "xxUSYaNnQHCzvfUNfWB8jA");
    }
}
