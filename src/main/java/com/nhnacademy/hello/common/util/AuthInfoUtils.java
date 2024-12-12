package com.nhnacademy.hello.common.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthInfoUtils {
    // 현재 로그인 상태와 로그인 정보를 쉽게 가져오도록 해주는 static 유틸 클래스

    private AuthInfoUtils() {
    }

    // 현재 로그인 되어 있는지 여부
    static public boolean isLogin(){
        return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    // 현재 로그인된 아이디, 로그인 안했으면 null
    static public String getUsername() {
        if(!isLogin()) {
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 현재 로그인된 role, 로그인 안했으면 null
    // ROLE_ADMIN, ROLE_MEMBER 이렇게 반환됨니다
    static public String getRole(){
        if(!isLogin()) {
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().findFirst().get().getAuthority();
    }
}
