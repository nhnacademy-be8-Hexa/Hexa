package com.nhnacademy.hello.controller.member;

import com.nhnacademy.hello.common.feignclient.HexaGateway;
import com.nhnacademy.hello.dto.member.MemberRegisterDTO;
import com.nhnacademy.hello.dto.member.MemberRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class MemberRegisterController {

    private final HexaGateway hexaGateway;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String registerForm() {
        return "member/register";
    }

    @PostMapping
    public String register(@Valid MemberRegisterDTO registerDTO,
                           BindingResult bindingResult,
                           Model model) {
        // 입력 형식 검증에서 걸리면 에러 보여주고 다시
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "member/register";
        }

        MemberRequestDTO requestDTO = new MemberRequestDTO(
                registerDTO.memberId(),
                passwordEncoder.encode(registerDTO.memberPassword()),
                registerDTO.memberName(),
                registerDTO.memberNumber(),
                registerDTO.memberEmail(),
                registerDTO.memberBirthAt(),
                null,
                "1",
                "1"
        );

        hexaGateway.createMember(requestDTO);

        return "redirect:/";
    }

}