package com.nhnacademy.hello.common.interceptor;

//import com.nhnacademy.hello.common.properties.JwtProperties;
//import feign.RequestTemplate;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class FeignClientJwtInterceptorTest {
//    private FeignClientJwtInterceptor interceptor;
//    private HttpServletRequest request;
//    private JwtProperties jwtProperties;
//    private RequestTemplate requestTemplate;
//
//    @BeforeEach
//    void setUp() {
//        request = mock(HttpServletRequest.class);
//        jwtProperties = mock(JwtProperties.class);
//        requestTemplate = new RequestTemplate();
//
//        interceptor = new FeignClientJwtInterceptor(request, jwtProperties);
//
//        // JwtProperties 설정
//        when(jwtProperties.getHeaderString()).thenReturn("Authorization");
//        when(jwtProperties.getTokenPrefix()).thenReturn("Bearer");
//    }
//
//    @Test
//    void apply_WithValidTokenInCookie_ShouldAddHeader() {
//        // given: 쿠키에 JWT 토큰이 존재
//        Cookie[] cookies = {new Cookie("token", "test-jwt-token")};
//        when(request.getCookies()).thenReturn(cookies);
//
//        // when: 인터셉터 실행
//        interceptor.apply(requestTemplate);
//
//        // then: 헤더에 토큰 추가 확인
//        assertTrue(requestTemplate.headers().containsKey("Authorization"));
//        assertEquals("Bearer test-jwt-token", requestTemplate.headers().get("Authorization").iterator().next());
//    }
//
//    @Test
//    void apply_NoTokenInCookies_ShouldNotAddHeader() {
//        // given: 쿠키에 토큰이 없음
//        Cookie[] cookies = {new Cookie("other", "value")};
//        when(request.getCookies()).thenReturn(cookies);
//
//        // when: 인터셉터 실행
//        interceptor.apply(requestTemplate);
//
//        // then: 헤더에 아무것도 추가되지 않음
//        assertFalse(requestTemplate.headers().containsKey("Authorization"));
//    }
//
//    @Test
//    void apply_NullCookies_ShouldNotAddHeader() {
//        // given: 쿠키가 null인 경우
//        when(request.getCookies()).thenReturn(null);
//
//        // when: 인터셉터 실행
//        interceptor.apply(requestTemplate);
//
//        // then: 헤더에 아무것도 추가되지 않음
//        assertFalse(requestTemplate.headers().containsKey("Authorization"));
//    }
//}