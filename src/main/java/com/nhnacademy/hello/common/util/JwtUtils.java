package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.common.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


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

}