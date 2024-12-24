package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.HexaGateway;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/mypage/edit")
    public String editPage(
            Model model
    ){
        MemberDTO member = hexaGateway.getMember(AuthInfoUtils.getUsername());
        MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                null,
                member.memberName(),
                member.memberNumber(),
                member.memberEmail(),
                member.memberBirthAt(),
                null,
                null
        );

        model.addAttribute("updateDTO", updateDTO);

        return "member/edit";
    }

    @PostMapping("/mypage/edit")
    public String edit(
        @Valid MemberUpdateDTO updateDTO,
        BindingResult bindingResult,
        Model model
    ){
        // 입력 형식 검증에서 걸리면 에러 보여주고 다시
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for(ObjectError error : bindingResult.getAllErrors()) {
                errors.add(error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            model.addAttribute("updateDTO", updateDTO);
            return "member/edit";
        }

        hexaGateway.updateMember(AuthInfoUtils.getUsername(), updateDTO);

        return "redirect:/mypage";
    }

}
