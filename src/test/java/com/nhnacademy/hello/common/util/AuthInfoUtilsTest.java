package com.nhnacademy.hello.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AuthInfoUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isLogin() {
        Authentication auth = new TestingAuthenticationToken("testUser", "password", "ROLE_MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(AuthInfoUtils.isLogin());
    }

    @Test
    void isLogin_null() {
        assertFalse(AuthInfoUtils.isLogin());
    }

    @Test
    void getUsername() {
        Authentication auth = new TestingAuthenticationToken("testUser", "password", "ROLE_MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Assertions.assertEquals("testUser", AuthInfoUtils.getUsername());
    }

    @Test
    void getUsername_null() {
        assertNull(null, AuthInfoUtils.getUsername());
    }

    @Test
    void getRole() {
        Authentication auth = new TestingAuthenticationToken("testUser", "password", "ROLE_MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Assertions.assertEquals("ROLE_MEMBER", AuthInfoUtils.getRole());
    }

    @Test
    void getRole_null() {
        assertNull(null, AuthInfoUtils.getRole());
    }

}