package com.nhnacademy.hello.common.filter;

import com.nhnacademy.hello.common.feignclient.auth.TokenAdapter;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.CookieUtil;
import com.nhnacademy.hello.common.util.JwtUtils;
import com.nhnacademy.hello.dto.jwt.AccessRefreshTokenDTO;
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
import java.util.Objects;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final JwtUtils jwtUtils;
    private final TokenAdapter tokenAdapter;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtProperties jwtProperties, JwtUtils jwtUtils, TokenAdapter tokenAdapter, CookieUtil cookieUtil) {
        this.jwtProperties = jwtProperties;
        this.jwtUtils = jwtUtils;
        this.tokenAdapter = tokenAdapter;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        // access 토큰와 refresh 토큰을 각각 가져옴

        String accessToken = cookieUtil.getCookieValue(request, "accessToken");

        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");


        // 접근 토큰이 있나
        if (accessToken != null) {
            // 접근토큰이 만료가 안되었나
            if (jwtUtils.validateAccessToken(accessToken)) {
                // 리프레시 토큰이 있으나 없으나 권한주기

                String username = jwtUtils.getUsernameFromAccessToken(accessToken);
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(jwtUtils.getRoleFromAccessToken(accessToken)));

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // 컨텍스트에 인증 설정 (security 인증)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 접근토큰이 만료된 경우
            else {
                // 리프레시 토큰이 있는 경우
                if (refreshToken != null) {

                    String sendRefreshToken = jwtProperties.getTokenPrefix() + " " + refreshToken;

                    // 접근 토큰의 이름과 권한이 리프레시 토큰의 이름과 권한과 같은가 그리고 토큰이 블랙리스트에 없는가

                    String accessTokenUserName = jwtUtils.getUsernameFromAccessToken(accessToken);
                    String refreshTokenUserName = jwtUtils.getUsernameFromRefreshToken(refreshToken);
                    String accessTokenUserRole = jwtUtils.getRoleFromAccessToken(accessToken);
                    String refreshTokenUserRole = jwtUtils.getRoleFromAccessToken(accessToken);
                    Boolean blacklist = tokenAdapter.isTokenBlackListed(sendRefreshToken);

                    if (jwtUtils.getUsernameFromAccessToken(accessToken).equals(jwtUtils.getUsernameFromRefreshToken(refreshToken)) &&
                            jwtUtils.getRoleFromAccessToken(accessToken).equals(jwtUtils.getRoleFromRefreshToken(refreshToken)) &&
                            (!tokenAdapter.isTokenBlackListed(sendRefreshToken)))
                    {
                        // 기존 리프레시 토큰으로 새로 발급
                        AccessRefreshTokenDTO accessRefreshTokenDTO = tokenAdapter.reissueAccessRefreshToken(sendRefreshToken);

                        accessToken = accessRefreshTokenDTO.accessToken();
                        refreshToken = accessRefreshTokenDTO.refreshToken();

                        cookieUtil.addResponseAccessTokenCookie(response, accessToken, jwtProperties.getRefreshTokenExpirationTime());
                        cookieUtil.addResponseRefreshTokenCookie(response, refreshToken, jwtProperties.getRefreshTokenExpirationTime());


                        // 권한 넣어줌
                        String username = jwtUtils.getUsernameFromAccessToken(accessToken);
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(jwtUtils.getRoleFromAccessToken(accessToken)));

                        // 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        // 컨텍스트에 인증 설정 (security 인증)
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                    // 없으면 접근 권한 없게 설정
                }
            }
        }
        // 접근토큰이 없음

        filterChain.doFilter(request, response);
    }
}
