package com.nhnacademy.hello.common.security.OAuth2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaycoUserService {

    @Value("${payco.user.cancel.api.url}")
    private String apiUrl;  // URL을 application.properties에서 관리

    /**
     * 개인정보 제공 동의 철회 API 호출
     * @param clientId - 발급받은 CLIENT ID
     * @param accessToken - 사용자 access token
     * @return - API 요청 성공 여부
     */
    public boolean revokeOfferAgreement(String clientId, String accessToken) {
        // RestTemplate 인스턴스를 생성합니다.
        RestTemplate restTemplate = new RestTemplate();

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("client_id", clientId);
        headers.set("access_token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 본문은 비어있으므로 null로 설정합니다.
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            // POST 요청을 보내고 응답을 ResponseEntity로 받습니다.
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            // 응답 본문을 확인하여 성공 여부 판단 (응답 본문이 성공 메시지인지를 체크)
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                // 응답이 "SUCCESS" 메시지인지를 체크하여 성공 여부 판단
                return responseBody != null && responseBody.contains("\"resultMessage\":\"SUCCESS\"");
            }
        } catch (Exception e) {
            // 예외가 발생한 경우 실패 처리
            e.printStackTrace();
        }

        return false;  // 실패한 경우 false 반환
    }
}
