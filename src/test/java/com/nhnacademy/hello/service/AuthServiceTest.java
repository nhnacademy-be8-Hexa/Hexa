package com.nhnacademy.hello.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;

import com.nhnacademy.hello.image.tool.TokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    private final String authUrl = "http://authserver.com";
    private final String authBody = "{\"username\":\"test\",\"password\":\"secret\"}";

    @Mock
    private RestTemplate restTemplate;

    // 실제 ObjectMapper 사용 (JSON 파싱 검증)
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authService = new AuthService(authUrl, authBody, restTemplate, objectMapper);
    }

    @Test
    @DisplayName("requestToken() - 성공")
    void requestToken_Success() throws Exception {
        // 준비: auth 서버가 반환할 JSON 응답 (토큰 정보 포함)
        String jsonResponse = "{\"access\": { \"token\": { \"id\": \"abc123\", \"expires\": \"2025-12-31T23:59:59Z\" } } }";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        // restTemplate.exchange(...)가 위 ResponseEntity를 반환하도록 stub 처리
        when(restTemplate.exchange(
                eq(authUrl + "/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 실행
        TokenInfo tokenInfo = authService.requestToken();

        // 검증
        assertNotNull(tokenInfo);
        assertEquals("abc123", tokenInfo.getId());
        assertEquals(Instant.parse("2025-12-31T23:59:59Z"), tokenInfo.getExpires());
    }

    @Test
    @DisplayName("requestToken() - 실패 (HTTP 상태 코드 오류)")
    void requestToken_Failure_StatusCode() {
        // 준비: 실패 상태코드 예) BAD_REQUEST
        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                eq(authUrl + "/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 실행 및 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.requestToken());
        assertTrue(exception.getMessage().contains("Failed to authenticate"));
    }

    @Test
    @DisplayName("requestToken() - 실패 (응답 본문이 null)")
    void requestToken_Failure_NullBody() {
        // 준비: HTTP 상태 OK이지만 응답 본문은 null
        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(authUrl + "/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 실행 및 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.requestToken());
        assertTrue(exception.getMessage().contains("Failed to authenticate"));
    }

    @Test
    @DisplayName("requestToken() - 실패 (JSON 파싱 에러)")
    void requestToken_Failure_ParsingError() {
        // 준비: 올바르지 않은 JSON 문자열 응답
        String invalidJson = "Invalid JSON";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(authUrl + "/tokens"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 실행 및 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.requestToken());
        assertTrue(exception.getMessage().contains("Failed to parse token response"));
    }
}