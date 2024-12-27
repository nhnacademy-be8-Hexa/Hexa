package com.nhnacademy.hello.common.interceptor;

import com.nhnacademy.hello.common.util.AuthInfoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!AuthInfoUtils.isLogin()) {
            response.sendRedirect("/login");
            return false;
        }
        if (!"ADMIN".equals(AuthInfoUtils.getRole())) {
            response.sendRedirect("/index");
            return false;
        }
        return true;
    }
}

