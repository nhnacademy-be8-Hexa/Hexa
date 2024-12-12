package com.nhnacademy.hello.common.filter;

import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtProperties jwtProperties, JwtUtils jwtUtils) {
        this.jwtProperties = jwtProperties;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = null;

//        String header = jwtProperties.getHeaderString();

        // 요청 헤더에서 액세스 토큰을 읽기
//        final String authorizationHeader = request.getHeader(header);

//        if (authorizationHeader != null && authorizationHeader.startsWith(header)) {
//            jwtToken = authorizationHeader.substring(header.length());
//        }

        // 쿠키에서 토큰 가져오기
        jwtToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if(jwtToken != null && jwtUtils.validateToken(jwtToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 토큰을 파싱 해서 security context에 인증 정보를 저장한다
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(jwtUtils.getRoleFromToken(jwtToken)));

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 컨텍스트에 인증 설정 (security 인증)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
