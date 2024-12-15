package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.common.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    JwtProperties jwtProperties;

    private String token;

    @BeforeEach
    void setUp() {
        token = Jwts.builder()
                .claim("userId", "testMember")
                .claim("role", "ROLE_MEMBER")
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    @Test
    void getUsernameFromToken() {
        Assertions.assertEquals("testMember", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void getAuthoritiesFromToken() {
        Assertions.assertEquals("ROLE_MEMBER", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void validateToken() {
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void invalidToken() {
        assertFalse(jwtUtils.validateToken(token+"asdasfa"));
    }
}