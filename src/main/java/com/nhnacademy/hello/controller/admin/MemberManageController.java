package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class MemberManageController {
    private final MemberAdapter memberAdapter;

    // 멤버 전체 조회 (페이징 및 검색 처리)
    @GetMapping
    public String getMembers(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam(required = false) String search,
                             Model model) {
        try {
            List<MemberDTO> members = memberAdapter.getMembers(page - 1, pageSize, search);
            model.addAttribute("members", members);

            long totalCount = memberAdapter.getMemberCount(search).getBody();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search);
        } catch (Exception e) {
            model.addAttribute("error", "회원 데이터를 불러오는 중 오류가 발생했습니다.");
        }

        return "admin/memberManage";
    }

    // 특정 회원 상세 조회
    @GetMapping("/{memberId}")
    public String getMemberDetail(@PathVariable String memberId, Model model) {
        MemberDTO member = memberAdapter.getMember(memberId);
        model.addAttribute("member", member);
        return "admin/memberDetail";
    }

    // 특정 회원 수정 폼
    @GetMapping("/update/{memberId}")
    public String getUpdateForm(@PathVariable String memberId, Model model) {
        try {
            MemberDTO member = memberAdapter.getMember(memberId);

            if (member == null) {
                model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "redirect:/admin/members"; // 목록 페이지로 리다이렉트
            }

            List<RatingDTO> ratings = memberAdapter.getRatings();
            List<MemberStatusDTO> memberStatuses = memberAdapter.getMemberStatus();

            // MemberDTO를 MemberUpdateDTO로 변환
            MemberUpdateDTO updateDTO = convertToUpdateDTO(member);

            model.addAttribute("updateDTO", updateDTO);  // 수정용 DTO 전달
            model.addAttribute("ratings", ratings);
            model.addAttribute("memberStatuses", memberStatuses);

            // 추가로 lastLoginAt과 createdAt 전달
            model.addAttribute("memberCreatedAt", member.memberCreatedAt());
            model.addAttribute("memberLastLoginAt", member.memberLastLoginAt());

            return "admin/memberUpdateForm";
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/admin/members";
        }
    }

    // MemberDTO를 MemberUpdateDTO로 변환하는 메소드
    private MemberUpdateDTO convertToUpdateDTO(MemberDTO member) {
        return new MemberUpdateDTO(
                null,  // 패스워드는 수정하지 않으므로 null로 설정
                member.memberName(),
                member.memberNumber(),
                member.memberEmail(),
                member.memberBirthAt(),
                member.rating() != null ? String.valueOf(member.rating().ratingId()) : null,
                member.memberStatus() != null ? String.valueOf(member.memberStatus().statusId()) : null
        );
    }

    // 회원 수정 요청 처리
    @PostMapping("/update/{memberId}")
    public String updateMember(@PathVariable String memberId,
                               @Valid @ModelAttribute("updateDTO") MemberUpdateDTO memberUpdateDTO,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (ObjectError error : bindingResult.getAllErrors()) {
                errors.add(error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);

            List<RatingDTO> ratings = memberAdapter.getRatings();
            List<MemberStatusDTO> memberStatuses = memberAdapter.getMemberStatus();
            model.addAttribute("ratings", ratings);
            model.addAttribute("memberStatuses", memberStatuses);

            return "redirect:/admin/members";
        }

        try {
            memberAdapter.updateMember(memberId, memberUpdateDTO);

            return "redirect:/admin/members";
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 저장하는 중 오류가 발생했습니다.");
            return "admin/memberUpdateForm";
        }
    }
    // 특정 회원 상태 변경 요청 (탈퇴 처리)
    @PutMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<Void> deactivateMember(@PathVariable String memberId) {
        try {
            // MemberUpdateDTO를 사용하여 상태를 "탈퇴"로 변경
            MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                    null,    // 비밀번호
                    null,    // 이름
                    null,    // 연락처
                    null,    // 이메일
                    null,    // 생일
                    null,    // 등급
                    "3"      // 탈퇴 상태 ID
            );
            memberAdapter.updateMember(memberId, updateDTO); // PUT 요청 전송
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
