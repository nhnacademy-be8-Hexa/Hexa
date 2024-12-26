package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.HexaGateway;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MemberManagementController {
    private final HexaGateway hexaGateway;

    @GetMapping("/admin/memberManage")
    public String memberManage(Model model){
        // 로그인 검사
        if(!AuthInfoUtils.isLogin()){
            return "redirect:/login";
        }

        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = hexaGateway.getMember(AuthInfoUtils.getUsername());

        // 관리자인지 검사
        if(!"ADMIN".equals(memberDTO.memberRole())){
            return "redirect:/index"; //
        }

        model.addAttribute("member",memberDTO);

        return "admin/memberManage";
    }
}
