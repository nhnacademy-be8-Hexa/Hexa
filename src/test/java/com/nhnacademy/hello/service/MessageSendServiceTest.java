package com.nhnacademy.hello.service;

import com.nhnacademy.hello.common.feignclient.dooray.MessageSendClient;
import com.nhnacademy.hello.dto.dooray.MessagePayloadDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSendServiceTest {

    @Mock
    private MessageSendClient messageSendClient;

    @InjectMocks
    private MessageSendService messageSendService;

    @Test
    @DisplayName("sendMessage: should call messageSendClient and return its result")
    void sendMessage_Success() {
        // Given
        String messageContent = "Hello World";
        String expectedResponse = "OK"; // client가 반환할 예상 결과

        when(messageSendClient.sendMessage(
                any(MessagePayloadDTO.class),
                eq(3204376758577275363L),
                eq(3972679502704436132L),
                eq("xxUSYaNnQHCzvfUNfWB8jA")
        )).thenReturn(expectedResponse);

        // When
        String actualResponse = messageSendService.sendMessage(messageContent);

        // Then
        assertEquals(expectedResponse, actualResponse);

        // 또한, 전달된 MessagePayloadDTO에 설정된 값들을 검증
        ArgumentCaptor<MessagePayloadDTO> captor = ArgumentCaptor.forClass(MessagePayloadDTO.class);
        verify(messageSendClient).sendMessage(
                captor.capture(),
                eq(3204376758577275363L),
                eq(3972679502704436132L),
                eq("xxUSYaNnQHCzvfUNfWB8jA")
        );
        MessagePayloadDTO sentPayload = captor.getValue();
        assertEquals("Notification", sentPayload.getBotName());
        assertEquals(messageContent, sentPayload.getText());
    }
}