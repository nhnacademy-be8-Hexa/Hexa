package com.nhnacademy.hello.controller.index;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping(value = {"/index.html","/"})
    public String index(
            Model model
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 로그인 하지 않을 경우 현재 인증 정보 객체는 익명 인증 정보 (AnonymousAuthenticationToken) 이다
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            model.addAttribute("userId", authentication.getName());
            model.addAttribute("role", authentication.getAuthorities().stream().findFirst().get().getAuthority());
        }

        return "index/index";
    }
}
