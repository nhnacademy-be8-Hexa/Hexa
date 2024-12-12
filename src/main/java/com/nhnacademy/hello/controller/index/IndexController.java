package com.nhnacademy.hello.controller.index;

import com.nhnacademy.hello.common.util.AuthInfoUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping(value = {"/index.html","/"})
    public String index(
            Model model
    ){

        // 로그인 하지 않을 경우 현재 인증 정보 객체는 익명 인증 정보 (AnonymousAuthenticationToken) 이다
        if(AuthInfoUtils.isLogin()) {
            // AuthInfoUtils 사용 예시
            model.addAttribute("userId", AuthInfoUtils.getUsername());
            model.addAttribute("role", AuthInfoUtils.getRole());
        }

        return "index/index";
    }
}
