package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class MemberManageController {
    private final MemberAdapter memberAdapter;

    // 멤버 전체 조회
    @GetMapping
    public String getMembers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String search,
                               Model model) {
        List<MemberDTO> members;

        if (search != null && !search.trim().isEmpty()) {
            // 특정 아이디로 검색
            members = memberAdapter.getMembers(page, search);
        } else {
            // 전체 회원 조회
            members = memberAdapter.getMembers(page, null);
        }        model.addAttribute("members", members);
        return "admin/memberManage"; // 멤버 관리 페이지
    }

    // 특정 회원 상세 조회
    @GetMapping("/{memberId}")
    public String getMember(@PathVariable String memberId, Model model) {
        // 특정 멤버 정보를 가져와서 모델에 추가
        MemberDTO member = memberAdapter.getMember(memberId);
        model.addAttribute("member", member);
        return "admin/memberDetail"; // 상세 정보 페이지
    }

    // 특정 회원 수정 페이지
    @GetMapping("/update/{memberId}")
    public String getUpdateForm(@PathVariable String memberId, Model model){
        MemberDTO member = memberAdapter.getMember(memberId);
        model.addAttribute("member",member);
        return "admin/memberUpdateForm";
    }

    // 멤버 정보 수정 (상태 변경 포함)
    @PostMapping("/{memberId}")
    public String updateMember(@PathVariable String memberId,
                               @ModelAttribute @Valid MemberUpdateDTO memberUpdateDTO) {
        // 멤버 정보를 수정
        ResponseEntity<MemberDTO> response = memberAdapter.updateMember(memberId, memberUpdateDTO);

        if (response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/admin/members"; // 수정 성공 시 목록 페이지로 이동
        }

        return "error"; // 실패 시 에러 페이지
    }
}
