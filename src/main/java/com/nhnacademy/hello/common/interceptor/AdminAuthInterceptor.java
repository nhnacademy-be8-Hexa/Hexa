package com.nhnacademy.hello.common.interceptor;

import com.nhnacademy.hello.common.util.AuthInfoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 검사
        if(!AuthInfoUtils.isLogin()){
            response.sendRedirect("/login");
            return false;
        }

        // 관리자인지 권한 검사
        if ("ADMIN".equals(AuthInfoUtils.getRole())){
            return true;
        } else {
            response.sendRedirect("/index");
            return false;
        }

    }
}
