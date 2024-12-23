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

        if(AuthInfoUtils.isLogin()) {
            model.addAttribute("userId", AuthInfoUtils.getUsername());
            model.addAttribute("role", AuthInfoUtils.getRole());
        }

        return "index/index";
    }
}
