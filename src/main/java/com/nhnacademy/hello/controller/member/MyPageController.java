package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.HexaGateway;
import com.nhnacademy.hello.common.properties.JwtProperties;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final HexaGateway hexaGateway;

    @GetMapping("/mypage")
    public String myPage(
            Model model
    ) {

        // 먼저 로그인했는지 검사
        if(!AuthInfoUtils.isLogin()){
            // 로그인 하지 않았을 경우, 로그인 화면으로 이동
            return "redirect:/login";
        }

        // 현재 로그인된 아이디를 이용해서 api 로부터 멤버 정보 받아옴
        MemberDTO member = hexaGateway.getMember(AuthInfoUtils.getUsername());
        model.addAttribute("member", member);

        return "member/myPage";
    }

}
