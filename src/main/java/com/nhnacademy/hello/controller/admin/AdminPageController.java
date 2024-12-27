package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminPageController {
    private final MemberAdapter memberAdapter;

    @GetMapping("/admin")
    public String adminPage(Model model){

        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());
        model.addAttribute("member",memberDTO);

        return "admin/adminPage";
    }
}
