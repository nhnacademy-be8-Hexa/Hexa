package com.nhnacademy.hello.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.hello.dto.toss.TossPayment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TossService {

    @Value("${toss.secret.key}")
    private String tossSecretKey;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public ResponseEntity<?> confirmPayment(
            String paymentKey,
            String orderId,
            int amount
    ){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", "Basic " + tossSecretKey)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\"paymentKey\":\"" + paymentKey + "\",\"orderId\":\"" + orderId + "\",\"amount\":" + amount + "}"))
                .build();

        try {

            // HttpClient를 사용하여 요청 보내기
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // 승인 처리 실패
            if(response.statusCode() != HttpStatus.OK.value()) {
                throw new IOException(response.body());
            }

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // 승인 처리 완료
            return ResponseEntity
                    .status(response.statusCode())
                    .body(objectMapper.readValue(response.body(), TossPayment.class));

        } catch (IOException | InterruptedException e) {
            // 시스템 예외
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 처리 중 내부 오류가 발생했습니다.");
        }
    }

}
