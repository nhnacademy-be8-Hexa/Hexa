package com.nhnacademy.hello.common.util;

import com.nhnacademy.hello.common.properties.JwtProperties;
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
        token = "eyJ0eXAiOiJCZWFyZXIiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiJ0ZXN0TWVtYmVyIiwicm9sZSI6IlJPTEVfTUVNQkVSIiwiaWF0IjoxNzMzODk5NjEwLCJleHAiOjE3MzM5MDMyMTB9.fsf_a4jz8SrjQ1_11b2uyxoN70yF0TMTzrL8HAqo6yQ";
    }

    @Test
    void getUsernameFromToken() {
        Assertions.assertEquals("testMember", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void getAuthoritiesFromToken() {
        Assertions.assertEquals("ROLE_MEMBER", jwtUtils.getAuthoritiesFromToken(token).getFirst().getAuthority());
    }
}