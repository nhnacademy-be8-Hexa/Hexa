package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
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
        List<MemberDTO> members;
        int totalPages;

        try {
            // FeignClient를 통해 페이징 및 검색 조건으로 회원 목록 조회
            members = memberAdapter.getMembers(page - 1, pageSize, search);

            // 전체 페이지 수 계산 (현재는 mock 값 사용, 실제 전체 회원 수 API 사용 가능)
            int totalCount = members.size(); // 실제 totalCount를 가져오는 API가 있다면 교체
            totalPages = (int) Math.ceil((double) totalCount / pageSize);

            model.addAttribute("members", members);
            model.addAttribute("currentPage", page); // 요청된 page 값을 모델에 전달
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search);
        } catch (Exception e) {
            model.addAttribute("error", "회원 데이터를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("members", List.of());
        }

        return "admin/memberManage"; // 멤버 관리 페이지
    }


    // 특정 회원 상세 조회
    @GetMapping("/{memberId}")
    public String getMemberDetail(@PathVariable String memberId, Model model) {
        MemberDTO member = memberAdapter.getMember(memberId);

        model.addAttribute("member",member);

        return "admin/memberDetail";
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
