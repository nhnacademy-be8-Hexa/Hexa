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

        String accessToken = cookieUtil.getCookieValue(request,"accessToken");

        String refreshToken = cookieUtil.getCookieValue(request,"refreshToken");

        // refresh 토큰은 무조건 있어야함



        // 토큰이 null 이 아니고 유효기간이 아직 남아있으면
        if(refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)){

            String sendRefreshToken = jwtProperties.getTokenPrefix() + " " + refreshToken;

            String serverRefreshToken = tokenAdapter.getServerToken(sendRefreshToken);

            // 서버에 저장된 유저의 토큰이 null이 아니고 유저가 보낸 토큰과 값이 같으면
            if(serverRefreshToken != null && Objects.equals(serverRefreshToken, refreshToken)){

                // refresh token 이 만료되지 않았으면 (이 안쪽 부분까지 들어오면 인증처리되게 만들어야 함)
                if(jwtUtils.validateRefreshToken(refreshToken)){

                    // access token 만료 된 경우
                    if((accessToken == null)  || (!jwtUtils.validateAccessToken(accessToken)) ){

                        AccessRefreshTokenDTO accessRefreshTokenDTO = tokenAdapter.reissueAccessRefreshToken(sendRefreshToken);

                        accessToken = accessRefreshTokenDTO.accessToken();
                        refreshToken = accessRefreshTokenDTO.refreshToken();
                    }

                    // access token 만료된경우나 안된경우나 인증절차 거치고 쿠키 추가

                    String username = jwtUtils.getUsernameFromAccessToken(accessToken);
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(jwtUtils.getRoleFromAccessToken(accessToken)));

                    // 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // 컨텍스트에 인증 설정 (security 인증)
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    cookieUtil.addResponseAccessTokenCookie(response,accessToken,jwtProperties.getAccessTokenExpirationTime());
                    cookieUtil.addResponseRefreshTokenCookie(response,refreshToken,jwtProperties.getRefreshTokenExpirationTime());

                }
                else {
                    // 리프레시 토큰 지워서 보내기
                    cookieUtil.addResponseRefreshTokenCookie(response,refreshToken,0);
                }

            }
            else{
                // 서버에 저장된 유저의 토큰이 null 이면 이것도 만료된거니까  지워서 보내기
                // 서버 토큰이랑 리프레시 토큰이랑 다르면 서버쪽 토큰 지우고 그냥 보내서 그 계정으로 접속한 사람들 비로그인 상태로 만들기
                cookieUtil.addResponseRefreshTokenCookie(response,refreshToken,0);
                cookieUtil.addResponseAccessTokenCookie(response,accessToken,0);
            }

        }

        filterChain.doFilter(request, response);
    }
}
