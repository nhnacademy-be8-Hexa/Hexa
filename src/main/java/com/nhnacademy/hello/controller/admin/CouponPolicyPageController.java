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

public class CouponPolicyPageController {

    private final MemberAdapter memberAdapter;

    @GetMapping("/admin/coupon-policy")
    public String adminPage(Model model) {

        // 로그인 검사
        if (!AuthInfoUtils.isLogin()) {
            return "redirect:/login";
        }

        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        // 관리자인지 검사
        if (!"ADMIN".equals(memberDTO.memberRole())) {
            return "redirect:/index";
        }

        model.addAttribute("member", memberDTO);

        return "admin/coupon-policy";
    }

}