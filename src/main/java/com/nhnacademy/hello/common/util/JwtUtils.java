package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;

    // JWT에서 사용자 ID 추출
    public String getUsernameFromAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getAccessSecret())
                    .build()
                    .parseClaimsJws(token) // 만약 토큰이 만료되었다면 여기서 예외 발생
                    .getBody();

            // Claims에서 원하는 값을 추출
            return claims.get("userId", String.class);
        } catch (ExpiredJwtException e) {
            // 토큰은 만료되었지만 Claims는 여전히 읽을 수 있음
            Claims claims = e.getClaims();
            return claims.get("userId", String.class);
        } catch (JwtException e) {
            // 다른 JWT 관련 예외 처리
            throw new RuntimeException("Invalid or malformed JWT token", e);
        }
    }

    // JWT에서 권한 추출
    public String getRoleFromAccessToken(String token) {

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getAccessSecret())
                    .build()
                    .parseClaimsJws(token) // 만약 토큰이 만료되었다면 여기서 예외 발생
                    .getBody();

            // Claims에서 원하는 값을 추출
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            // 토큰은 만료되었지만 Claims는 여전히 읽을 수 있음
            Claims claims = e.getClaims();
            return claims.get("role", String.class);
        } catch (JwtException e) {
            // 다른 JWT 관련 예외 처리
            throw new RuntimeException("Invalid or malformed JWT token", e);
        }

    }

    // jwt가 올바른지 검증
    public Boolean validateAccessToken(String token) {
        try{
            Jwts.parser().setSigningKey(jwtProperties.getAccessSecret()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        }
    }


    // JWT에서 사용자 ID 추출
    public String getUsernameFromRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getRefreshSecret())
                    .build()
                    .parseClaimsJws(token) // 만약 토큰이 만료되었다면 여기서 예외 발생
                    .getBody();

            // Claims에서 원하는 값을 추출
            return claims.get("userId", String.class);
        } catch (ExpiredJwtException e) {
            // 토큰은 만료되었지만 Claims는 여전히 읽을 수 있음
            Claims claims = e.getClaims();
            return claims.get("userId", String.class);
        } catch (JwtException e) {
            // 다른 JWT 관련 예외 처리
            throw new RuntimeException("Invalid or malformed JWT token", e);
        }
    }

    // JWT에서 권한 추출
    public String getRoleFromRefreshToken(String token) {

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getRefreshSecret())
                    .build()
                    .parseClaimsJws(token) // 만약 토큰이 만료되었다면 여기서 예외 발생
                    .getBody();

            // Claims에서 원하는 값을 추출
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            // 토큰은 만료되었지만 Claims는 여전히 읽을 수 있음
            Claims claims = e.getClaims();
            return claims.get("role", String.class);
        } catch (JwtException e) {
            // 다른 JWT 관련 예외 처리
            throw new RuntimeException("Invalid or malformed JWT token", e);
        }

    }

    // jwt가 올바른지 검증
    public Boolean validateRefreshToken(String token) {
        try{
            Jwts.parser().setSigningKey(jwtProperties.getRefreshSecret()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        }
    }

}