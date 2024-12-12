package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.common.properties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
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
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
    }

    // JWT에서 권한 추출
    public String getRoleFromToken(String token) {

        return Jwts.parser()
                        .setSigningKey(jwtProperties.getSecret())
                .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .get("role", String.class);

    }

    // jwt가 올바른지 검증
    public Boolean validateToken(String token) {
        try{
            Jwts.parser().setSigningKey(jwtProperties.getSecret()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException | ExpiredJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        }
    }

}
