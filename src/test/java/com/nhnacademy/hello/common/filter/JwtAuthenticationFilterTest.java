package com.nhnacademy.hello.common.filter;

//import com.nhnacademy.hello.common.properties.JwtProperties;
//import com.nhnacademy.hello.common.util.JwtUtils;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.Cookie;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.authentication.TestingAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;

//class JwtAuthenticationFilterTest {
//
//    private JwtProperties jwtProperties;
//    private JwtUtils jwtUtils;
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @BeforeEach
//    void setUp() {
//        jwtProperties = mock(JwtProperties.class);
//        jwtUtils = mock(JwtUtils.class);
//        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProperties, jwtUtils);
//
//        // SecurityContext 초기화
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
//        // given: MockHttpServletRequest와 MockHttpServletResponse 설정
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        FilterChain filterChain = mock(FilterChain.class);
//
//        // 쿠키 설정
//        Cookie tokenCookie = new Cookie("token", "valid-jwt-token");
//        request.setCookies(tokenCookie);
//
//        // JwtUtils 설정
//        when(jwtUtils.validateToken("valid-jwt-token")).thenReturn(true);
//        when(jwtUtils.getUsernameFromToken("valid-jwt-token")).thenReturn("testUser");
//        when(jwtUtils.getRoleFromToken("valid-jwt-token")).thenReturn("ROLE_MEMBER");
//
//        // when: 필터 실행
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then: SecurityContext에 인증 객체가 설정되었는지 확인
//        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
//        assertEquals("testUser", SecurityContextHolder.getContext().getAuthentication().getName());
//        assertEquals("ROLE_MEMBER", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
//
//        // FilterChain 호출 확인
//        verify(filterChain, times(1)).doFilter(request, response);
//    }
//
//    @Test
//    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws Exception {
//        // given: MockHttpServletRequest와 MockHttpServletResponse 설정
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        FilterChain filterChain = mock(FilterChain.class);
//
//        // 쿠키 설정
//        Cookie tokenCookie = new Cookie("token", "invalid-jwt-token");
//        request.setCookies(tokenCookie);
//
//        // JwtUtils 설정
//        when(jwtUtils.validateToken("invalid-jwt-token")).thenReturn(false);
//
//        // when: 필터 실행
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then: SecurityContext에 인증 객체가 설정되지 않았는지 확인
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//
//        // FilterChain 호출 확인
//        verify(filterChain, times(1)).doFilter(request, response);
//    }
//
//    @Test
//    void doFilterInternal_NoToken_ShouldNotSetAuthentication() throws Exception {
//        // given: MockHttpServletRequest와 MockHttpServletResponse 설정
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        FilterChain filterChain = mock(FilterChain.class);
//
//        // 쿠키 없음
//        request.setCookies();
//
//        // when: 필터 실행
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // then: SecurityContext에 인증 객체가 설정되지 않았는지 확인
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//
//        // FilterChain 호출 확인
//        verify(filterChain, times(1)).doFilter(request, response);
//    }
//
//    @Test
//    void doFilterInternal_WithValidToken_AlreadyAuthenticated() throws Exception {
//        // given: MockHttpServletRequest와 MockHttpServletResponse 설정
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        FilterChain filterChain = mock(FilterChain.class);
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new TestingAuthenticationToken("testUser", "testPassword", "ROLE_MEMBER")
//        );
//
//        // 쿠키 설정
//        Cookie tokenCookie = new Cookie("token", "valid-jwt-token");
//        request.setCookies(tokenCookie);
//
//        // JwtUtils 설정
//        when(jwtUtils.validateToken("valid-jwt-token")).thenReturn(true);
//
//        // when: 필터 실행
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // FilterChain 호출 확인
//        verify(filterChain, times(1)).doFilter(request, response);
//    }
//}